package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.swing.rx.EventStatus;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class Directory {

    private final ValueHolder<Path> directory = ValueHolder.create();

    private final EventStatus.Publisher select = EventStatus.create();

    private final ValueHolder<List<Path>> history = ValueHolder.create();
    private final HistoryControl historyControl;

    @Nullable
    private Runnable reload = null;

    public Directory(@NotNull HistoryControl historyControl) {
        this.historyControl = historyControl;
        this.historyControl.updated().subscribe(this.history::put);
    }

    public Path get() {
        return directory.get();
    }

    public void put(Path dir) {
        directory.put(dir);
    }

    public Supplier<Path> getter() {
        return this::get;
    }

    /////////////////////////////

    public Observable<Path> onChange() {
        return directory.observable();
    }

    public Observable<EventStatus> onSelectionNeeded() {
        return select.asObservable();
    }

    public void select() {
        select.onNext();
    }

    public void select(int[] indices) {
        final Path dir = getPathFromHistory(indices);
        if (dir == null) {
            return;
        }
        directory.put(dir);
    }

    public void removePath(int[] indices) {
        final Path dir = getPathFromHistory(indices);
        if (dir == null) {
            return;
        }
        historyControl.remove(dir);
    }

    @Nullable
    public Path getPathFromHistory(int[] indices) {
        if (indices == null || indices.length == 0) {
            return null;
        }
        final List<Path> paths = history.get();
        if (paths.isEmpty()) {
            return null;
        }
        return paths.get(indices[0]);
    }

    Observable<List<Path>> history() {
        return history.observable();
    }

    /////////////////////////////

    public void reload() {
        if (reload != null) {
            reload.run();
        }
    }

    public void setReload(@Nullable Runnable reload) {
        this.reload = reload;
    }

    /////////////////////////////

    public void openExplorer() {
        try {
            Desktop.getDesktop().open(directory.get().toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
