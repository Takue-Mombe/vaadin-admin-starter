package com.adminpro.application;

import com.adminpro.domain.ReportConfig;
import com.adminpro.infrastructure.repo.ReportConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ReportService {

    private final ReportConfigRepository repository;

    public ReportService(ReportConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ReportConfig> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReportConfig> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repository.findAll();
        }
        String value = filter.trim();
        return repository.findByReportNameContainingIgnoreCaseOrOwnerNameContainingIgnoreCaseOrCadenceContainingIgnoreCase(
            value,
            value,
            value
        );
    }

    public ReportConfig save(ReportConfig reportConfig) {
        if (reportConfig.getLastGenerated() == null) {
            reportConfig.setLastGenerated(LocalDate.now());
        }
        return repository.save(reportConfig);
    }

    public void delete(ReportConfig reportConfig) {
        repository.delete(reportConfig);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }
}
