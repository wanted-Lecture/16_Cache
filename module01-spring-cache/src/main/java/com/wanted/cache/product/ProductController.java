package com.wanted.cache.product;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/before/products/{id}")
    public Product getProductBefore(@PathVariable Long id) {
        return productService.getProductBefore(id);
    }

    @GetMapping("/before/products")
    public ProductResponse searchBefore(
            @RequestParam(defaultValue = "popular") String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        return productService.searchBefore(keyword, category, minPrice, maxPrice);
    }

    @GetMapping("/after/products/{id}")
    public Product getProductAfter(@PathVariable Long id) {
        return productService.getProductAfter(id);
    }

    @GetMapping("/after/products")
    public ProductResponse searchAfter(
            @RequestParam(defaultValue = "popular") String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        return productService.searchAfter(keyword, category, minPrice, maxPrice);
    }
}
