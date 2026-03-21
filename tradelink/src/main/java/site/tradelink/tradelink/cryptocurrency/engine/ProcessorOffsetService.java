package site.tradelink.tradelink.cryptocurrency.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.entity.ProcessorOffset;
import site.tradelink.tradelink.cryptocurrency.repository.ProcessorOffsetRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorOffsetService {

    private final ProcessorOffsetRepository offsetRepository;

    // offset 전진
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void advanceOffset(String ticker, long seq) {
        ProcessorOffset offset = offsetRepository.findByTicker(ticker)
                .orElse(null);

        if (offset == null) {
            offset = ProcessorOffset.create(ticker);
            offset.advance(seq);
            offsetRepository.save(offset);
        } else {
            offset.advance(seq);
        }
    }
}
