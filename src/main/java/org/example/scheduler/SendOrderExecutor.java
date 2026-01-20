package org.example.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.CommonSchedulingTask;
import org.example.service.SendOrderService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendOrderExecutor implements CommonSchedulingTask {
    private final SendOrderService sendOrderService;

    @Override
    public int getThreadCount() {
        return 3;
    }

    @Override
    public int execute() {
        int processedCount = sendOrderService.processOutboxMessages();
        log.info("processedCount={}, tread: {}", processedCount, Thread.currentThread().getName());
        return processedCount;
    }
}
