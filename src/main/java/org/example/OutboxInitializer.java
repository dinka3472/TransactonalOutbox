package org.example;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.outbox.processor.defaults.properties.AbstractOutboxProcessorProperties;
import org.example.persistence.repo.OutboxOffsetRepository;
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

    /**
     * Инициализирует запись в таблице {@code outbox_offsets}
     * для заданного {@code topic} и виртуальной партиции {@code partition},
     * если такая запись ещё не существует.
     * <p>
     * Число виртуальных партиций задается с запасом,
     * что позволяет в дальнейшем увеличивать параллельную обработку
     * без изменения структуры данных и перераспределения сообщений.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        properties.stream()
                .collect(Collectors.toMap(
                        AbstractOutboxProcessorProperties::getTopic,
                        AbstractOutboxProcessorProperties::getVirtualPartitions))
                .forEach((topic, partitions) -> {
                    for (int p = 0; p < partitions; p++) {
                        repository.insertOffsetsIfNotExists(topic, p);
                    }
                });
    }
}

