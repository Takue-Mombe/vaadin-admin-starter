package com.adminpro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_metric")
public class AnalyticsMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Metric name is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String metricName;

    @NotNull(message = "Metric value is required")
    @DecimalMin(value = "0.0", message = "Metric value cannot be negative")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal metricValue = BigDecimal.ZERO;

    @Size(max = 12)
    @Column(length = 12)
    private String unit = "K";

    @NotBlank(message = "Period is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String period = "This week";

    @NotBlank(message = "Trend is required")
    @Size(max = 16)
    @Column(nullable = false, length = 16)
    private String trend = "UP";

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AnalyticsMetric() {
    }

    public AnalyticsMetric(String metricName,
                           BigDecimal metricValue,
                           String unit,
                           String period,
                           String trend) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.unit = unit;
        this.period = period;
        this.trend = trend;
    }

    public Long getId() {
        return id;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public BigDecimal getMetricValue() {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue) {
        this.metricValue = metricValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
