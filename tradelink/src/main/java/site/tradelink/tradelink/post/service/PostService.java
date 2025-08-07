package site.tradelink.tradelink.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostTransactionalService transactionalService;
}
