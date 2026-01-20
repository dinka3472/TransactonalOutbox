package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "outbox")
@Getter
@Setter
public class OutboxProperties {

    /** Включение/отключение Outbox-процессора */
    private boolean enabled = true;

    /** Размер batch для отправки сообщений в Kafka */
    private int batchSize = 100;

    /** Таймаут блокировки offset, пока воркер обрабатывает партицию */
    private Duration lockTimeout = Duration.ofSeconds(30);

    /** Таймаут для пустой партиции, после которого offset снова доступен */
    private Duration emptyTimeout = Duration.ofSeconds(5);

    /** Количество виртуальных партиций на каждый топик */
    private int partitions = 8;

    /** Список топиков, которые обрабатывает outbox */
    private List<String> topics;

}
