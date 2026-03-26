# 📈 TradeLink (환율 정보 및 투자 커뮤니티 서비스)

> **"캐시 없이 RDB의 극한까지, 그리고 OS/네트워크 레벨의 병목까지 파고든 아키텍처 연구 프로젝트"**

TradeLink는 실시간 환율 데이터와 투자 의견을 공유하는 커뮤니티 서비스입니다. 
본 프로젝트는 단순한 비즈니스 로직 구현을 넘어, 특정 시간대에 트래픽이 폭발적으로 집중되는 상황(Hot Spot)을 인위적으로 가정하고, OS(TCP Buffer), DB(Page Latch), Framework(Spring Core) 계층에서 발생하는 병목을 규명하고 아키텍처 관점에서 해결하는 것을 목적으로 한 연구형 프로젝트입니다.

<br>

## 🛠️ Tech Stack
* **Language:** Java 25 (Virtual Thread 적극 활용)
* **Framework:** Spring Boot 4.0, Spring Data JPA
* **Database:** MySQL 8.0, HikariCP
* **Test & Profiling:** nGrinder, MySQL General Log

<br>

## 🔥 핵심 트러블슈팅 및 아키텍처 설계 (Key Engineering)

### 1. 좋아요 시스템의 Hot Spot 병목 해결 (CQRS & Event Sourcing)
**[문제 상황]**
특정 게시글에 1초 만에 1만 개의 좋아요가 몰리는 상황에서, 단순 `SELECT 후 INSERT` 로직이나 비관적 락(Pessimistic Lock)은 DB 커넥션 고갈을 유발합니다. 특히 인덱스가 걸린 물리적 리프 페이지(Leaf Page)에 쓰기 요청이 집중되며 MySQL의 **Page Latch 경합(Spinlock)**이 발생해 서버가 마비되는 현상이 발생합니다.

**[해결 과정]**
* **Append-Only 로그 전환:** `like_events` 테이블을 만들어 모든 요청을 쓰기 전용(Write-Only)으로 순차 기록하여 Latch 경합을 회피했습니다.
* **CQRS 패턴 적용:** API 서버는 배치로 BULK INSERT 후 즉시 `202 Accepted`를 반환하고, 백그라운드 잡(Job)이 `like_status`와 `post_stats`를 비동기로 갱신하도록 쓰기와 읽기를 분리했습니다.
* **DLQ (Dead Letter Queue) 도입:** 백그라운드 워커 처리 중 특정 이벤트 예외 시 전체 배치가 무한 재시도되는 **Poison Pill 현상**을 방지하기 위해, 실패 이벤트를 DLQ로 격리(`REQUIRES_NEW` 트랜잭션 전파 활용)하여 배치 흐름의 안정성을 확보했습니다.

<br>

### 2. `@Transactional(readOnly=true)`가 유발한 커넥션 풀 고갈 해결
**[문제 상황]**
실시간 환율 조회 API 부하 테스트(VUser 100, Pool 10) 중 DB 쿼리 실행 시간은 0.1ms 미만임에도, 커넥션 획득 대기 시간(Pending)이 급증하며 평균 응답 시간이 400ms까지 지연되는 현상을 발견했습니다.

**[원인 분석]**
MySQL 환경에서 Spring의 `@Transactional(readOnly=true)`가 열릴 때 발생하는 불필요한 세션 설정 쿼리(`SET session transaction read only`, `autocommit=0` 등)가 네트워크 I/O를 발생시켜 커넥션 점유 시간을 늘리고 있었습니다.

**[해결 과정 및 결과]**
* **전파 속성 최적화:** `Propagation.SUPPORTS`를 적용하여 상위 트랜잭션이 없을 때는 트랜잭션 없이 동작하도록 변경했습니다.
* **Spring Data JPA 프록시 우회:** `SimpleJpaRepository`에 걸려있는 기본 트랜잭션을 회피하기 위해 Custom Repository(JPQL)를 구현하여 프레임워크 강제 로직을 분리했습니다.
* **결과:** 불필요한 부가 쿼리 제거로 커넥션 획득 대기 시간을 **0.3ms ➔ 0.03ms (1/10 수준)로 단축**하고, 전체 응답 시간을 **최대 82% 개선**했습니다.

<br>

### 3. 비동기 큐 & Virtual Thread를 통한 SSE HOL Blocking 방지
**[문제 상황]**
실시간 환율 브로드캐스팅(SSE) 시, 네트워크가 느린 단 한 명의 클라이언트 때문에 OS의 **TCP Send Buffer**가 가득 차면, 해당 브로드캐스트 스레드가 Blocking되어 전체 정상 유저들의 데이터 수신까지 멈추는 **Head-of-Line(HOL) Blocking** 현상을 발견했습니다.

**[해결 과정]**
* **격리(Isolation) 아키텍처 설계:** 브로드캐스트 스레드는 각 클라이언트의 비동기 큐(`Queue`)에 데이터를 넣기만 하고 즉시 다음으로 넘어가도록 분리했습니다.
* **Virtual Thread (단일 소비자):** 클라이언트마다 큐의 데이터를 소비하여 `emitter.send()`를 수행하는 독립적인 Virtual Thread 전송 루프를 할당했습니다. Blocking이 발생해도 Carrier Thread를 반납하므로 스레드 고갈이 발생하지 않습니다.
* **Backpressure 제어:** 클라이언트 큐가 가득 차면(`offer` 실패) 즉시 연결을 끊어버려, 느린 클라이언트가 서버 메모리를 점유하는 현상을 방지했습니다.

<br>

### 4. HttpClient 내부 Executor 최적화 (Carrier Thread Pinning 방지)
**[최적화]**
환율 정보를 주기적으로 가져오는 스케줄러를 여러 통화에 대해 병렬 호출하도록 Virtual Thread로 구성했습니다. 이때 `RestClient` 내부 `HttpClient`가 사용하는 디폴트 Executor가 일반 스레드 풀일 경우 I/O 대기 시 Carrier Thread가 점유되는 현상을 막기 위해, HttpClient 빌더에 `Executors.newVirtualThreadPerTaskExecutor()`를 직접 주입하여 **전 구간 Non-Blocking(Carrier Thread 관점) 구조를 완성**했습니다.
