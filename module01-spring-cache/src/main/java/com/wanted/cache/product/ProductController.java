package com.wanted.cache.product;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
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

    /* @CachePut
    *   해당 메서드에 Cache 가 존재 하더라도 메서드를 실행한다.
    *   강제로 DB를 다시 조회하고, 조회된 결과를 캐시에 덮어쓴다.
    * */
    @PostMapping("/after/products/{id}/refresh")
    public Product refreshProduct(@PathVariable Long id) {
        return  productService.refreshProduct(id);
    }

    /* Comment
    *   @CacheEvict 는 캐시 항목을 제거한다.
    *
    * */
    @DeleteMapping("/after/products/{id}/cache")
    public ResponseEntity<Void> evictProduct(@PathVariable Long id) {
        productService.evictProduct(id);

        return ResponseEntity.noContent().build();
    }

    /* Comment
    *   재고변경이 일어나는 상태를 가정한다.
    *   - 재고가 변경되면 상세 캐시와 검색 캐시 모두 오래된 데이터를 가질 수 있다.
    *   -
    * */
    @PatchMapping("/after/products/{id}/stock")
    public Product changeStock(@PathVariable Long id, @RequestParam int stock) {
        return productService.changeStock(id, stock);
    }
}
