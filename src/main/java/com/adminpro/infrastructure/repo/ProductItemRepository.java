package com.adminpro.infrastructure.repo;

import com.adminpro.domain.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductItemRepository extends JpaRepository<ProductItem, Long> {

    List<ProductItem> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name,
            String sku,
            String category
    );

    long countByStatus(String status);
}
