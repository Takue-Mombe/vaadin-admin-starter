package com.adminpro.application;

import com.adminpro.domain.OrderRecord;
import com.adminpro.infrastructure.repo.OrderRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRecordRepository repository;

    public OrderService(OrderRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OrderRecord> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrderRecord> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repository.findAll();
        }
        String value = filter.trim();
        return repository.findByOrderNumberContainingIgnoreCaseOrCustomerContainingIgnoreCaseOrPlanNameContainingIgnoreCase(
            value,
            value,
            value
        );
    }

    public OrderRecord save(OrderRecord orderRecord) {
        return repository.save(orderRecord);
    }

    public void delete(OrderRecord orderRecord) {
        repository.delete(orderRecord);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalRevenue() {
        return repository.findAll().stream()
            .map(OrderRecord::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
