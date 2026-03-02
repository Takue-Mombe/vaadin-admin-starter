package com.adminpro.application;

import com.adminpro.domain.SupportMessage;
import com.adminpro.infrastructure.repo.SupportMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MessageService {

    private final SupportMessageRepository repository;

    public MessageService(SupportMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SupportMessage> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SupportMessage> search(String filter) {
        if (filter == null || filter.isBlank()) {
            return repository.findAll();
        }
        String value = filter.trim();
        return repository.findBySenderNameContainingIgnoreCaseOrSubjectContainingIgnoreCaseOrChannelContainingIgnoreCase(
            value,
            value,
            value
        );
    }

    public SupportMessage save(SupportMessage message) {
        if (message.getReceivedAt() == null) {
            message.setReceivedAt(LocalDateTime.now());
        }
        return repository.save(message);
    }

    public void delete(SupportMessage message) {
        repository.delete(message);
    }

    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        return repository.countByStatus(status);
    }
}
