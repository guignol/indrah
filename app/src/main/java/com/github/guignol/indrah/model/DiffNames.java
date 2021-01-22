package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiffNames {

    private final String before;
    private final String after;

    public DiffNames(@Nullable String before, @Nullable String after) {
        this.before = before;
        this.after = after;
        any();
    }

    @NotNull
    public String before() {
        if (before != null) {
            return before;
        }
        throw new RuntimeException("NO DIFF TARGET.before");
    }

    @NotNull
    public String after() {
        if (after != null) {
            return after;
        }
        throw new RuntimeException("NO DIFF TARGET.after");
    }

    @NotNull
    public String any() {
        if (after != null) {
            return after;
        }
        if (before != null) {
            return before;
        }
        throw new RuntimeException("NO DIFF TARGET");
    }
}
