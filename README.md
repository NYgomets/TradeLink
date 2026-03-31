# 📈 TradeLink (환율 정보 및 투자 커뮤니티 서비스)

> **"기술이 아닌 문제가 먼저다. 비즈니스 정합성을 지키면서 프레임워크와 OS 레벨의 병목을 파고든 아키텍처 고도화 프로젝트"**

TradeLink는 실시간 가상화폐 모의투자, 환율 데이터 조회 및 투자 커뮤니티 기능을 제공하는 서비스입니다. 단순히 프레임워크를 사용하는 것에 그치지 않고, 대용량 트래픽 상황에서 발생하는 DB 커넥션 병목, 실시간 통신에서의 OS/네트워크 레벨 Blocking, 그리고 금융 도메인에서 가장 중요한 데이터 정합성(잔고, 호가) 문제를 구조적으로 해결하는 데 집중했습니다.

<br>

## 🛠️ Tech Stack
* **Language:** Java 25 (Virtual Thread 적극 활용)
* **Framework:** Spring Boot 4.0, Spring Data JPA
* **Database:** MySQL 8.0, HikariCP
* **Test & Profiling:** nGrinder, MySQL General Log

<br>

## 🔥 핵심 트러블슈팅 및 아키텍처 설계 (Key Engineering)

### 1. 실시간 체결 알림과 호가 Broadcast: SSE Head-of-Line (HOL) Blocking 해결
**[문제 상황]**
실시간 호가 및 체결 알림을 위해 SSE를 도입했습니다. Servlet 3.0 덕분에 스레드 고갈 문제는 없었으나, 네트워크가 느린 단 한 명의 클라이언트 때문에 OS의 TCP Send Buffer가 가득 차면, 동기식 forEach 루프 전체가 대기 상태에 빠지는 HOL Blocking 현상을 발견했습니다.

**[해결 과정]**
* **비동기 큐 & Virtual Thread 아키텍처:** 브로드캐스트 스레드는 각 클라이언트의 큐(Queue)에 이벤트를 넣기만 하고 즉시 반환되도록 구조를 분리했습니다.
* **독립적인 단일 소비자 전송:** 클라이언트마다 할당된 Virtual Thread가 독립적으로 큐에서 이벤트를 꺼내 전송(emitter.send())합니다. Blocking이 발생해도 Carrier Thread를 반납하므로 전체 서버 리소스를 낭비하지 않으며, Thread-Safe하지 않은 SseEmitter의 동시 접근 문제를 구조적으로 차단했습니다.
* **Backpressure 제어:** 클라이언트의 큐가 가득 찰 경우 즉시 연결을 해제하여, 느린 클라이언트가 서버 메모리를 무한정 고갈시키는 상황을 방지했습니다.

<br>

### 2. 환율 서비스 - @Transactional(readOnly=true)의 이면
**[문제 상황]**
실시간 환율 조회 API 부하 테스트(VUser 100, Pool 10) 중 DB 쿼리 실행 시간은 0.1ms 미만임에도, 커넥션 획득 대기 시간(Pending)이 급증하며 평균 응답 시간이 400ms까지 지연되는 현상을 발견했습니다.

**[원인 분석]**
단순 조회 메서드에 적용된 @Transactional(readOnly=true)가 MySQL 환경에서 새로운 트랜잭션을 시작하며 불필요한 세션 설정 쿼리(SET session transaction read only, autocommit=0 등)를 유발했고, 이로 인한 네트워크 I/O가 커넥션 점유 시간을 늘리고 있었습니다.

**[해결 과정 및 결과]**
* **전파 속성 최적화:** `Propagation.SUPPORTS`를 적용하여 상위 트랜잭션이 없을 때는 트랜잭션 없이 동작하도록 변경했습니다.
* **Spring Data JPA 프록시 우회:** `SimpleJpaRepository`에 걸려있는 기본 트랜잭션을 회피하기 위해 Custom Repository(JPQL)를 구현하여 프레임워크 강제 로직을 분리했습니다.
* **결과:** 불필요한 부가 쿼리 제거로 커넥션 획득 대기 시간을 **0.3ms ➔ 0.03ms (1/10 수준)로 단축**하고, 전체 응답 시간을 **최대 82% 개선**했습니다.

<br>

### 3. 시장가 주문 체결 시스템
**[문제 상황]**
동기식으로 접수와 체결을 동시에 처리할 경우 응답 지연이 발생하며, 실제 거래소의 비동기 체결 모델과도 맞지 않았습니다. 또한 비동기 처리 시 잔고 이중 차감이나 오래된 호가(stale data)로 인한 오체결 위험이 존재했습니다.

**[해결 과정]**
* **CQRS & Append-only 이벤트 소싱:** 주문 접수 시 쓰기 전용 이벤트 테이블에 PK만으로 INSERT 하여 Page Latch 경합을 방지하고 즉시 202 Accepted를 반환하도록 설계했습니다. 체결 로직은 종목(Ticker)별로 할당된 Virtual Thread가 병렬로 처리합니다.
* **원자적 예약 차감 보장:** 동시 주문 시 잔고 음수를 막기 위해, 별도의 SELECT 없이 단일 UPDATE 쿼리의 WHERE 조건으로 잔고 확인과 차감을 원자적으로 처리했습니다.
* **트랜잭션 격리 (Poison Pill 방지):** 슬리피지 예외로 체결 트랜잭션이 롤백되더라도, 예약금 환불 로직을 트랜잭션 외부로 분리하고 커서 이동 로직을 REQUIRES_NEW로 격리하여 단일 주문 실패가 전체 배치 흐름을 멈추지 않도록 구성했습니다.
* **CAS 기반 호가 정합성 유지:** ConcurrentHashMap과 CAS(Compare-And-Swap) 연산을 활용해 다중 스레드 환경에서 호가 캐시를 안전하게 업데이트하고, TTL(5초)을 검증하여 오래된 호가로 인한 체결을 차단했습니다.
