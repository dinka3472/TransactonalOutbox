package org.example.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.CommonSchedulingTask;
import org.example.config.schedule.properties.SendOrderSchedulerProperties;
import org.example.processor.OutboxProcessor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendOrderSchedulerTask implements CommonSchedulingTask {
    private final OutboxProcessor outboxProcessor;
    private final SendOrderSchedulerProperties sendOrderSchedulerProperties;

    @Override
    public Duration getPeriod() {
        return sendOrderSchedulerProperties.getPeriod();
    }

    @Override
    public boolean isFixRate() {
        return sendOrderSchedulerProperties.isFixRate();
    }

    @Override
    public int getThreadCount() {
        return sendOrderSchedulerProperties.getThreadCount();
    }

    @Override
    public int execute() {
        int processedCount = outboxProcessor.processOutboxMessages();
        log.debug("processedCount={}, tread: {}", processedCount, Thread.currentThread().getName());
        return processedCount;
    }
}
