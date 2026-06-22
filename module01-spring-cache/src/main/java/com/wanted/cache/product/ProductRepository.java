package com.wanted.cache.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
        select *
        from products
        where (:keyword is null or lower(name) like lower(concat('%', :keyword, '%')))
          and (:category is null or category = :category)
          and (:minPrice is null or price >= :minPrice)
          and (:maxPrice is null or price <= :maxPrice)
        order by popularity desc
        limit 30
        """, nativeQuery = true)
    List<Product> search(String keyword, String category, Integer minPrice, Integer maxPrice);
}
