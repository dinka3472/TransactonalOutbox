package org.example.processor.defaults.properties;

import lombok.Getter;
import lombok.Setter;

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
     * Количество партиций на топик
     */
    private Integer partitions;
    private String topic;

    public void applyDefaults(OutboxDefaultsProperties d) {
        if (batchSize == null) batchSize = d.getBatchSize();
        if (lockTimeout == null) lockTimeout = d.getLockTimeout();
        if (emptyTimeout == null) emptyTimeout = d.getEmptyTimeout();
    }
}
