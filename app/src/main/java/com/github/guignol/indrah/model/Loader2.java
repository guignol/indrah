package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Loader2<INPUT, RETURN> {
    void load(@NotNull INPUT trigger, @NotNull Consumer<RETURN> consumer);
}
