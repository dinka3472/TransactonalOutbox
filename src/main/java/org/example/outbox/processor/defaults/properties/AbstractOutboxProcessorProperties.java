package org.example.outbox.processor.defaults.properties;

import lombok.Getter;
import lombok.Setter;
import org.example.usecasses.constants.OutboxErrorStrategyType;

import java.time.Duration;

@Getter
@Setter
public abstract class AbstractOutboxProcessorProperties {

    /**
     * Размер batch для отправки сообщений в Kafka
     */
    private Integer batchSize;

    /**
     * Таймаут блокировки offset, пока воркер обрабатывает партицию
     */
    private Duration lockTimeout;

    /**
     * Таймаут для пустой партиции, после которого offset снова доступен
     */
    private Duration emptyTimeout;

    /**
     * Количество виртуальных партиций на топик (для параллельной обработки)
     */
    private Integer virtualPartitions;

    /**
     * Топик
     */
    private String topic;

    /**
     * Стратегия обработки ошибок
     */
    private OutboxErrorStrategyType errorStrategy;

    public void applyDefaults(OutboxDefaultsProperties d) {
        if (batchSize == null) batchSize = d.getBatchSize();
        if (lockTimeout == null) lockTimeout = d.getLockTimeout();
        if (emptyTimeout == null) emptyTimeout = d.getEmptyTimeout();
        if (virtualPartitions == null) virtualPartitions = d.getVirtualPartitions();
        if (errorStrategy == null) errorStrategy = d.getErrorStrategy();
    }
}
