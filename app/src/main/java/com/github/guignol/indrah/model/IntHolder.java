package com.github.guignol.indrah.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class IntHolder {
    private final int exclusiveMinimum;
    private final AtomicInteger offset;

    public IntHolder(int exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
        this.offset = new AtomicInteger(exclusiveMinimum);
    }

    public void set(int value) {
        offset.set(value);
    }

    public int getOrDefault(Supplier<Integer> defaultValue) {
        final int saved = offset.get();
        if (exclusiveMinimum < saved) {
            return saved;
        } else {
            final Integer newValue = defaultValue.get();
            offset.set(newValue);
            return newValue;
        }
    }

    public void reset() {
        offset.set(exclusiveMinimum);
    }
}
