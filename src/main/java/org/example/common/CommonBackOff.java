package org.example.common;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class CommonBackOff {
    private static final int INITIAL_INTERVAL = 1000;
    private static final double MULTIPLIER = 1.5;
    private static final int MAX_INTERVAL = 9000;

    private AtomicReference<BackOffExecution> execution;

    public CommonBackOff() {
        execution = new AtomicReference<>();
        reset();
    }

    public long nextBackOff() {
        return execution.get().nextBackOff();
    }

    private void reset() {
        execution.set(createBackOffExecution());
    }

    @SneakyThrows
    public void sleep(final int processed) {
        if (processed == 0) {
            long nextBackOff = nextBackOff();
            log.debug("Sleeping for {} milliseconds, thread: {}", nextBackOff, Thread.currentThread().getName());
            Thread.sleep(nextBackOff);
        } else {
            reset();
        }
    }

    private BackOffExecution createBackOffExecution() {
        final ExponentialBackOff backOff = new ExponentialBackOff(INITIAL_INTERVAL, MULTIPLIER);
        backOff.setMaxInterval(MAX_INTERVAL);
        return backOff.start();
    }
}
