package com.wanted.cache.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/* Comment
*   @EnableCaching 어노테이션이 있어야
*   @Cacheable, @CachePut, @CacheEvict, @Caching
*   둥 캐시 관련 어노테이션이 동작할 수 있다.
*   Spring 은 해당 어노테이션이 붙은 메서드를 프록시로 감싸고
*   메서드 호출 전 후에 캐시가 있는 지를 확인한다.
* */
@Configuration
@EnableCaching
public class LocalCacheConfig {

    private static final List<String> CACHE_NAMES = List.of(
            CacheNames.PRODUCT_DETAIL,
            CacheNames.PRODUCT_SEARCH
    );

    @Bean
    CacheManager cacheManager() {
        // CacheManager는 추상화 객체
        // 실제 구현체는 CaffeineCacheManager 로 구성한다.
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 위에서 만든 cache name 대입
        cacheManager.setCacheNames(CACHE_NAMES);

        // 캐시 관련 설정
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        // 캐시 폭발을 방지하기 위해, 최대 캐시 항목 수 지정
                        .maximumSize(1_000)
                        // 오래 된 캐시가 무한히 남지 않게 만료 시간(TTL) 을 둔다.
                        .expireAfterAccess(Duration.ofMinutes(5)) // 5분 후 캐시 만료
                        // 캐시 히트 비율, 미스 비율 등을 지표로 볼 수있게 통계를 기록한다.
                        // prometheus 와 연동 가능
                        .recordStats()
                        // 크기 제한, 만료 등으로 제거 된 캐시 항목을 관찰할 수 있다.
                        // 커스텀 메서드 사용 가능
                        .removalListener(
                                (key, value, cause)
                                -> System.out.printf("cache removed: key=%s , cause=%s%n", key , cause))
        );

        /* Comment
        *   현재 cacheManager 설정은 모든 종류의 캐시가 5분 만료 시간을 가질 수 있다.
        *   하지만, 캐시의 종류에 따라서 TTL 설정은 달라져야 한다.
        * */
//        cacheManager.registerCustomCache(
//                "PRODUCT_ALL",
//                Caffeine.newBuilder()
//                        .maximumSize(5_000)
//                        .expireAfterAccess(Duration.ofHours(5))
//                        .build()
//                );

        return cacheManager;
    }
}
