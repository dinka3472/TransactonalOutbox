package org.example.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.common.CommonSchedulingTask;
import org.example.service.SendOrderService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendOrderExecutor implements CommonSchedulingTask {
    private final SendOrderService sendOrderService;

    @Override
    public int getThreadCount() {
        return 3;
    }

    @Override
    public int execute() {
        return sendOrderService.processOutboxMessages();
    }
}
