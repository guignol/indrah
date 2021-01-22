package com.github.guignol.indrah.mvvm.common;

import com.github.guignol.indrah.Preference;
import com.github.guignol.indrah.mvvm.ValueHolder;
import com.github.guignol.indrah.utils.ListUtils;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class HistoryControl {

    private final ValueHolder<List<Path>> histories = ValueHolder.createDefault(new ArrayList<>());

    public HistoryControl(Preference preference) {
        final List<String> history = preference.readOnly().history;
        if (history != null && !history.isEmpty()) {
            histories.put(history.stream().map(Paths::get).collect(Collectors.toList()));
        }
        // 永続化
        histories.observable()
                .map(paths -> paths.stream().map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toList()))
                .subscribe(strings -> preference.edit(data -> data.history = new ArrayList<>(strings)));
    }

    public Observable<List<Path>> updated() {
        return histories.observable().map(HistoryControl::updated);
    }

    @NotNull
    private static List<Path> updated(List<Path> histories) {
        final int size = histories.size();
        if (2 <= size) {
            // 2～
            // 逆リストの先頭（リストの末尾）は現在表示中なので履歴に出さない
            return ListUtils.reversed(histories).subList(1, size);
        } else {
            // 0 or 1
            return new ArrayList<>();
        }
    }

    public void add(final Path path) {
        search(path, (list, old) -> {
            if (old == null) {
                list.add(path);
            } else {
                // 既に存在する場合は並び替え
                list.remove(old);
                list.add(old);
            }
            histories.put(list);
        });
    }

    public void remove(Path path) {
        search(path, (list, old) -> {
            if (old == null) {
                return;
            } else {
                list.remove(old);
            }
            histories.put(list);
        });
    }

    private void search(Path path, BiConsumer<List<Path>, Path> consumer) {
        final List<Path> list = histories.get();
        final String pathString = path.toAbsolutePath().toString();
        final Path target = ListUtils.find(list, history -> history.toAbsolutePath().toString().equals(pathString));
        consumer.accept(list, target);
    }

    private boolean isEmpty() {
        return histories.get().isEmpty();
    }

    @Nullable
    public Path lastRepository() {
        if (isEmpty()) {
            return null;
        }
        return ListUtils.last(histories.get());
    }
}
