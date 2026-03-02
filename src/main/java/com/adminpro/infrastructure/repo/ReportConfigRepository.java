package com.adminpro.infrastructure.repo;

import com.adminpro.domain.ReportConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportConfigRepository extends JpaRepository<ReportConfig, Long> {

    List<ReportConfig> findByReportNameContainingIgnoreCaseOrOwnerNameContainingIgnoreCaseOrCadenceContainingIgnoreCase(
            String reportName,
            String ownerName,
            String cadence
    );

    long countByStatus(String status);
}
