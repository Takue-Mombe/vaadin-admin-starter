package com.adminpro.infrastructure.repo;

import com.adminpro.domain.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, Long> {

    List<OrderRecord> findByOrderNumberContainingIgnoreCaseOrCustomerContainingIgnoreCaseOrPlanNameContainingIgnoreCase(
            String orderNumber,
            String customer,
            String planName
    );

    long countByStatus(String status);
}
