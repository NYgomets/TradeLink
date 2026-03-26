package site.tradelink.tradelink.cryptocurrency.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {}
