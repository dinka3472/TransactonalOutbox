package org.example.common;

import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;

public class CommonBackOffContext implements AutoCloseable {

    public static final ThreadLocal<CommonBackOff> threadLocalScope = new ThreadLocal<>();

    public static CommonBackOff getBackOf() {
        return threadLocalScope.get();
    }

    public CommonBackOffContext(@NonNull final CommonBackOff backOff) {
        threadLocalScope.set(backOff);
    }


    @Override
    public void close() {
        threadLocalScope.remove();
    }
}
