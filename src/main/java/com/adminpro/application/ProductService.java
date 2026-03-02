package com.adminpro.application;

import com.adminpro.domain.ProductItem;
import com.adminpro.infrastructure.repo.ProductItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductItemRepository repository;

    public ProductService(ProductItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductItem> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ProductItem> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repository.findAll();
        }
        String value = filter.trim();
        return repository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            value,
            value,
            value
        );
    }

    public ProductItem save(ProductItem productItem) {
        return repository.save(productItem);
    }

    public void delete(ProductItem productItem) {
        repository.delete(productItem);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }
}
