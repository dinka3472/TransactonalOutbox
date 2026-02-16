package org.example.outbox.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.CommonSchedulingTask;
import org.example.config.schedule.properties.SendOrderSchedulerProperties;
import org.example.outbox.processor.OutboxProcessor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendOrderSchedulerTask implements CommonSchedulingTask {
    private static final int ERROR_CONST = -1;
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

    //TODO надо подумать над обработкой исключений. Сюда прокидываться должны ретраебл исключения
    // Что делать с неретраебл исключениями??? в топик ошибок? нарушится порядок обработки?

    @Override
    public int execute() {
        try {
            int processedCount = outboxProcessor.processOutboxMessages();
            log.debug("processedCount={}, tread: {}", processedCount, Thread.currentThread().getName());
            return processedCount;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ERROR_CONST;
        }
    }
}
