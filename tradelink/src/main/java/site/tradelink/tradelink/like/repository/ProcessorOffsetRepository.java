package site.tradelink.tradelink.like.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.ProcessorOffset;

import java.util.Optional;

@Repository
public interface ProcessorOffsetRepository extends JpaRepository<ProcessorOffset, Long> {
    Optional<ProcessorOffset> findByProcessorName(String processorName);
}
