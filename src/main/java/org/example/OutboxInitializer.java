package org.example;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.persistence.repo.OutboxOffsetRepository;
import org.example.processor.defaults.properties.AbstractOutboxProcessorProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OutboxInitializer {

    private final OutboxOffsetRepository repository;
    private final List<AbstractOutboxProcessorProperties> properties;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        properties.stream()
                .collect(Collectors.toMap(
                        AbstractOutboxProcessorProperties::getTopic,
                        AbstractOutboxProcessorProperties::getPartitions))
                .forEach((key, value) -> {

                    for (int p = 0; p < value; p++) {
                        repository.insertIfNotExists(key, p);
                    }
                });
    }
}

