package com.github.guignol.indrah.mvvm.diff;

import com.github.guignol.indrah.model.Diff;
import com.github.guignol.swing.rx.EventStatus;
import com.github.guignol.indrah.utils.DiffUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class DiffRepository {

    @NotNull
    private Supplier<Path> directory;

    private final PublishSubject<List<Diff>> loaded = PublishSubject.create();

    private final boolean cached;

    DiffRepository(@NotNull Supplier<Path> dir, boolean cached) {
        this.directory = dir;
        this.cached = cached;
    }

    public Observable<List<Diff>> asObservable() {
        return loaded.hide();
    }

    public void load() {
        loadWith().subscribe();
    }

    public Single<EventStatus> loadWith() {
        final Path path = directory.get();
        if (path == null) {
            return Single.just(EventStatus.NEXT);
        }
        return Single.create(e -> DiffUtils.diffs(path, cached)
                .subscribe(t -> {
                    loaded.onNext(t);
                    e.onSuccess(EventStatus.NEXT);
                }));
    }

    public static boolean equals(List<Diff> oldDiffs, List<Diff> newDiffs) {
        if (oldDiffs.size() != newDiffs.size()) {
            return false;
        }
        // 順序が同じ前提で、Diff全体と各hunkのヘッダーのみ確認する
        for (int i = 0; i < oldDiffs.size(); i++) {
            final Diff before = oldDiffs.get(i);
            final Diff after = newDiffs.get(i);
            if (!Diff.equals(before, after)) {
                return false;
            }
        }
        return true;
    }
}
