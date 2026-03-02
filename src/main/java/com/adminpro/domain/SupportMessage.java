package com.adminpro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_message")
public class SupportMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sender name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String senderName;

    @Email(message = "Invalid sender email")
    @Size(max = 140)
    @Column(length = 140)
    private String senderEmail;

    @NotBlank(message = "Subject is required")
    @Size(max = 140)
    @Column(nullable = false, length = 140)
    private String subject;

    @Size(max = 280)
    @Column(length = 280)
    private String preview;

    @NotBlank(message = "Channel is required")
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String channel = "Email";

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "UNREAD";

    @Column(nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    public SupportMessage() {
    }

    public SupportMessage(String senderName,
                          String senderEmail,
                          String subject,
                          String preview,
                          String channel,
                          String status,
                          LocalDateTime receivedAt) {
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.subject = subject;
        this.preview = preview;
        this.channel = channel;
        this.status = status;
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
