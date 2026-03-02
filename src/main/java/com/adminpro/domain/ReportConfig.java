package com.adminpro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "report_config")
public class ReportConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Report name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String reportName;

    @NotBlank(message = "Owner is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String ownerName;

    @NotBlank(message = "Cadence is required")
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String cadence = "Monthly";

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "READY";

    @NotNull(message = "Last generated date is required")
    @Column(nullable = false)
    private LocalDate lastGenerated = LocalDate.now();

    public ReportConfig() {
    }

    public ReportConfig(String reportName,
                        String ownerName,
                        String cadence,
                        String status,
                        LocalDate lastGenerated) {
        this.reportName = reportName;
        this.ownerName = ownerName;
        this.cadence = cadence;
        this.status = status;
        this.lastGenerated = lastGenerated;
    }

    public Long getId() {
        return id;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getCadence() {
        return cadence;
    }

    public void setCadence(String cadence) {
        this.cadence = cadence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getLastGenerated() {
        return lastGenerated;
    }

    public void setLastGenerated(LocalDate lastGenerated) {
        this.lastGenerated = lastGenerated;
    }
}
