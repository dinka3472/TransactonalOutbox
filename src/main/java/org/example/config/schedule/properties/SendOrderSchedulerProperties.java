package org.example.config.schedule.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "scheduler.send-order-executor")
@Getter
@Setter
public class SendOrderSchedulerProperties {

    private Duration period;
    private boolean fixRate;
    private int threadCount;
}
