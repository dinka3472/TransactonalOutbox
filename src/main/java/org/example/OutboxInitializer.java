package org.example;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.config.OutboxProperties;
import org.example.repo.OutboxOffsetRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxInitializer {

    private final OutboxOffsetRepository repository;
    private final OutboxProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        for (String topic : properties.getTopics()) {
            for (int p = 0; p < properties.getPartitions(); p++) {
                repository.insertIfNotExists(topic, p);
            }
        }
    }
}

