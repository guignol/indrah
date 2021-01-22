package com.github.guignol.indrah;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Step<T> {

    void byStep(int offset, List<T> subList);

    static <T> void down(@NotNull List<T> target, Step<T> step) {
        final int size = target.size();
        for (int i = 0; i < size; i++) {
            step.byStep(i, target.subList(i, size));
        }
    }
}