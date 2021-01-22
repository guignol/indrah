package com.github.guignol.indrah.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public interface ListLike<T> {
    int size();

    T get(int index);

    static <T> ListLike<T> from(@NotNull List<T> list) {
        return new ListLike<T>() {
            @Override
            public int size() {
                return list.size();
            }

            @Override
            public T get(int index) {
                return list.get(index);
            }
        };
    }

    static <T> int findIndex(@NotNull List<T> list, @NotNull Predicate<T> predicate) {
        return findIndex(from(list), predicate);
    }

    static <T> int findIndex(@NotNull ListLike<T> list, @NotNull Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            final T item = list.get(i);
            if (predicate.test(item)) {
                return i;
            }
        }
        return -1;
    }

    static <T> int findLastIndex(@NotNull List<T> list, @NotNull Predicate<T> predicate) {
        return findLastIndex(from(list), predicate);
    }

    static <T> int findLastIndex(@NotNull ListLike<T> list, @NotNull Predicate<T> predicate) {
        for (int i = list.size() - 1; i >= 0; i--) {
            final T item = list.get(i);
            if (predicate.test(item)) {
                return i;
            }
        }
        return -1;
    }
}
