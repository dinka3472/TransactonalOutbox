package org.example.common;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonBackOffWrp implements Runnable {

    @NonNull
    private final Runnable runnable;

    private final CommonBackOff backOff = new CommonBackOff();

    @Override
    public void run() {
        try (final CommonBackOffContext ignored = new CommonBackOffContext(backOff)) {
            runnable.run();
        }
    }
}
