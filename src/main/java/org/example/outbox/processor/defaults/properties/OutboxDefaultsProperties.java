package org.example.outbox.processor.defaults.properties;

import lombok.Getter;
import lombok.Setter;
import org.example.usecasses.constants.OutboxErrorStrategyType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "outbox.defaults")
@Getter
@Setter
public class OutboxDefaultsProperties {
    private int batchSize = 100;

    private Duration lockTimeout = Duration.ofSeconds(30);
    private Duration emptyTimeout = Duration.ofSeconds(5);
    private int virtualPartitions = 50;
    private OutboxErrorStrategyType errorStrategy = OutboxErrorStrategyType.STRICT;
}
