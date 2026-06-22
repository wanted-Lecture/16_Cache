package com.wanted.cache.cache;

public class CacheNames {
    
    /* Comment 
    *   CacheKeys 는 캐시 공간에 접근하는 key 값을 정의한다.
    *   CacheName 는 해당 공간이 더떤 종류의 데이터를 담는 공간이지에 대한
    *   표현을 한다. => 설명
    * */

    // 상품 ID 하나에 상품 상세가 매핑되는 캐시
    public static final String PRODUCT_DETAIL = "productDetail";

    // 검색이 조홥에 따라 검색 결과가 매핑되는 캐시
    public static final String PRODUCT_SEARCH = "productSearch";

    private CacheNames() {
    }
}
