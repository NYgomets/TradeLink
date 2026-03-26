package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.ProcessorOffset;

import java.util.Optional;

@Repository
public interface ProcessorOffsetRepository extends JpaRepository<ProcessorOffset, Long> {
    Optional<ProcessorOffset> findByTicker(String ticker);
}
