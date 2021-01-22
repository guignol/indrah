package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface Loader<T> {
    void load(@NotNull Consumer<T> consumer);
}
