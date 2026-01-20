package org.example.common;

import java.time.Duration;

public interface CommonSchedulingTask extends Runnable {

    int execute();

    default Duration getInitialDelay() {
        return Duration.ofMillis(5000);
    }

    default Duration getPeriod() {
        return Duration.ofMillis(1000);
    }

    default boolean isFixRate() {
        return true;
    }

    default int getThreadCount() {
        return 1;
    }

    @Override
    default void run() {
        final int processed = execute();
        CommonBackOffContext.getBackOf().sleep(processed);
    }
}
