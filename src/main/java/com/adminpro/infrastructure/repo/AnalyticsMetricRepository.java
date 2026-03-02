package com.adminpro.infrastructure.repo;

import com.adminpro.domain.AnalyticsMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsMetricRepository extends JpaRepository<AnalyticsMetric, Long> {

    List<AnalyticsMetric> findByMetricNameContainingIgnoreCaseOrPeriodContainingIgnoreCase(String metricName,
                                                                                             String period);

    long countByTrend(String trend);
}
