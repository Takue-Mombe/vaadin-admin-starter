package com.adminpro.application;

import com.adminpro.domain.AnalyticsMetric;
import com.adminpro.infrastructure.repo.AnalyticsMetricRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AnalyticsService {

    private final AnalyticsMetricRepository repository;

    public AnalyticsService(AnalyticsMetricRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsMetric> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AnalyticsMetric> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repository.findAll();
        }
        String value = filter.trim();
        return repository.findByMetricNameContainingIgnoreCaseOrPeriodContainingIgnoreCase(value, value);
    }

    public AnalyticsMetric save(AnalyticsMetric metric) {
        metric.setUpdatedAt(LocalDateTime.now());
        return repository.save(metric);
    }

    public void delete(AnalyticsMetric metric) {
        repository.delete(metric);
    }

    @Transactional(readOnly = true)
    public long countByTrend(String trend) {
        return repository.countByTrend(trend);
    }
}
