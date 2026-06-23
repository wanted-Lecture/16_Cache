package com.wanted.cache.product;

import com.wanted.cache.cache.CacheNames;
import com.wanted.cache.support.SlowSimulator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
// 해당 어노테이션은 이 서비스에서 반복 사용하는 기본 캐시 이름을 Class Level 에 지정해
// 메서드 마다 중복될 수 있는 문자열을 줄이는 용도로 사용한다.
@CacheConfig(cacheNames = CacheNames.PRODUCT_DETAIL)
public class ProductService {

    private final ProductRepository productRepository;
    private final SlowSimulator slowSimulator;

    public Product getProductBefore(Long id) {

        // 의도적 지연 발생
        slowSimulator.detailQueryLatency();

        return findProduct(id);
    }

    // id 로 조회용 헬퍼 메서드
    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("id가 없습니다!"));
    }

    // 상품 검색
    public ProductResponse searchBefore(
            String keyword, String category, Integer minPrice, Integer maxPrice
    ) {

        slowSimulator.searchQueryLatency();

        return searchProducts(keyword, category, minPrice, maxPrice);
    }

    // 내부 메서드
    private ProductResponse searchProducts(String keyword, String category, Integer minPrice, Integer maxPrice) {
        List<Product> products = productRepository.search(blankToNull(keyword), blankToNull(category), minPrice, maxPrice);
        return new ProductResponse(keyword, category, minPrice, maxPrice, products.size(), products);
    }

    // 검색 조건이 비어서 올 때(빈 문자열) null 로 변환해서 반환
    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // 상세 조회데 캐시 추가
    // key :  메서드 파라미터 id 변수를 캐시 접근 key 로 사용하겠다는 의미
    // condition : 메서드 실행 전에 평가되며, id 가 0이하면 캐시를 사용하지 않는다.
    // unless : 매서드 실행 후에 평가된다. 결과값 null 이면 캐시를 사용하지 않는다.
    @Cacheable(key = "#id", condition = "#id > 0 ", unless = "#result == null ")
    public Product getProductAfter(Long id) {
        // 의도적 지연 발생
        slowSimulator.detailQueryLatency();

        return findProduct(id);
    }

    // 검색으로 상품 조회하기
    @Cacheable(
            cacheNames = CacheNames.PRODUCT_SEARCH,
            key = "T(com.wanted.cache.cache.CacheKeys).search(#keyword, #category, #minPrice, # maxPrice)",
            condition = "#keyword != null && #keyword.length() >= 2", // 검색어 2글자 이하로 키를 만들면 키가 너무 많아질 수 있다.
            unless = "#result.totalCount() == 0"
    )
    public ProductResponse searchAfter(
            String keyword, String category, Integer minPrice, Integer maxPrice
    ) {
        slowSimulator.searchQueryLatency();

       return searchProducts(keyword, category, minPrice, maxPrice);
    }

    // CachePut 은 캐시 히트 여부와 관계 없이 메서드 본문을 실행한다.
    // 수정하기 위해서는 해당 캐시에 접근할 수 있는 key값이 필요
    @CachePut(key = "#id")
    public Product refreshProduct(Long id) {
        slowSimulator.detailQueryLatency();
        return findProduct(id);
    }

    // id 값에 해당하는 캐시 데이터를 무효화(제거) 한다.
    @CacheEvict(key = "#id")
    public void evictProduct(Long id) {
        System.out.println("======캐시 공간 무효화 진행 완료=======");
    }

    /* Comment
    *   @Caching 은 여러 캐시 작업을 한 번에 묶을 수 있다.
    *   재고 변경 등에 의한 캐시 재설정은 put 보다는 evict 을 사용해서 기존 캐실르 날리고
    *   새롭게 만드는 방법을 훨씬 많이 쓰게 된다.
    *   evict allEntries = true 는 PRODUCT_SEARCH 캐시 전체를 비우는 명령어이다.
    *   해당 명령어는 단순하고 안전하지만, PRODUCT_SEARCH 캐시가 많을수록 재생성 비용이 커질 수 있다.
    *   Trade-off 가 발생한다.
    * */
    @Transactional // dml 구문
    @Caching(
            put = @CachePut(key = "#id"),
            evict = @CacheEvict(cacheNames = CacheNames.PRODUCT_SEARCH, allEntries = true)
    )
    public Product changeStock(Long id, int stock) {

        Product product = findProduct(id);

        product.changeStock(stock);

        return product;
    }
}
