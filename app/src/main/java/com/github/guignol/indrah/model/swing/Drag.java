package com.github.guignol.indrah.model.swing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Drag<T> {

    @Nullable
    private T startItem = getNull();
    private boolean prepared = false;

    final public void prepare(T from) {
        // ドラッグ中も呼ばれ続けるので、startItemが更新されないようにする
        if (!prepared) {
            startItem = from;
            prepared = true;
        }
    }

    final public void reset() {
        startItem = getNull();
        prepared = false;
    }

    final public boolean isDragging() {
        return prepared && startItem != null && isValid(startItem);
    }

    final public T from() {
        return startItem;
    }

    @Nullable
    abstract protected T getNull();

    abstract protected boolean isValid(@NotNull T startItem);
}
