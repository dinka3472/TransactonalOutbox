package org.example.common;

public interface CommonSchedulingTask extends Runnable {
    int execute();

    default long getInitialDelay() {
        return  5000;
    }
    default long getPeriod() {
        return 1000;
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
