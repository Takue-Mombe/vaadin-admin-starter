package com.adminpro.infrastructure.repo;

import com.adminpro.domain.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findBySenderNameContainingIgnoreCaseOrSubjectContainingIgnoreCaseOrChannelContainingIgnoreCase(
            String senderName,
            String subject,
            String channel
    );

    long countByStatus(String status);
}
