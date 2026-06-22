package com.wanted.cache.product;

import java.util.List;

public record ProductResponse(
        String keyword,
        String category,
        Integer minPrice,
        Integer maxPrice,
        int totalCount,
        List<Product> products
) {
}
