package com.github.guignol.indrah.model.swing;

import org.jetbrains.annotations.NotNull;

public class IndexedDrag extends Drag<Integer> {

    @NotNull
    @Override
    final protected Integer getNull() {
        return -1;
    }

    @Override
    protected boolean isValid(@NotNull Integer startItem) {
        return getNull() < startItem;
    }
}