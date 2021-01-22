package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.swing.rx.EventStatus;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

public class DiffReload {

    public final Holder workspace;
    public final Holder index;

    public final Runnable withoutFocusChanged;
    public final Runnable withWorkFocused;
    public final Runnable withIndexFocused;

    public final EventStatus.Publisher onReload = EventStatus.create();

    public DiffReload(Supplier<Path> dir) {
        workspace = new Holder(dir, false);
        index = new Holder(dir, true);

        withoutFocusChanged = () -> workspace.repository.loadWith().subscribe(status -> {
            index.repository.load();
            onReload.onNext();
        });
        Function<Boolean, Runnable> reloadAndFocus = workRepoFocus -> {
            if (workRepoFocus) {
                return () -> index.repository.loadWith()
                        .flatMap(status -> workspace.repository.loadWith())
                        .subscribe(status -> {
                            workspace.focus.onNext();
                            onReload.onNext();
                        });
            } else {
                return () -> workspace.repository.loadWith()
                        .flatMap(status -> index.repository.loadWith())
                        .subscribe(status -> {
                            index.focus.onNext();
                            onReload.onNext();
                        });
            }
        };
        withWorkFocused = reloadAndFocus.apply(true);
        withIndexFocused = reloadAndFocus.apply(false);
    }

    public static class Holder {

        // レポジトリ
        public final DiffRepository repository;

        // フォーカス制御
        public final EventStatus.Publisher focus = EventStatus.create();

        Holder(Supplier<Path> dir, boolean cached) {
            repository = new DiffRepository(dir, cached);
        }
    }
}
