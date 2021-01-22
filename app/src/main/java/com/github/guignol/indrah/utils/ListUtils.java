package com.github.guignol.indrah.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ListUtils {

    public static <T> IntStream filteredIndices(List<T> list, List<T> filter, BiPredicate<T, T> predicate) {
        final IntStream.Builder builder = IntStream.builder();
        for (int i = 0; i < list.size(); i++) {
            final T value0 = list.get(i);
            for (T value1 : filter) {
                if (predicate.test(value0, value1)) {
                    builder.accept(i);
                    break;
                }
            }
        }
        return builder.build();
    }

    public static <T> List<T> filter(List<T> list, List<T> filter, BiPredicate<T, T> predicate) {
        List<T> ret = new ArrayList<>();
        for (T value0 : list) {
            for (T value1 : filter) {
                if (predicate.test(value0, value1)) {
                    ret.add(value0);
                    break;
                }
            }
        }
        return ret;
    }

    public static <T> boolean anyMatch(@NotNull List<T> list0, @NotNull List<T> list1, BiPredicate<T, T> predicate) {
        for (T value0 : list0) {
            for (T value1 : list1) {
                if (predicate.test(value0, value1)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean allMatch(@NotNull List<T> list0, @NotNull List<T> list1, BiPredicate<T, T> predicate) {
        for (T value0 : list0) {
            for (T value1 : list1) {
                if (!predicate.test(value0, value1)) {
                    return false;
                }
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> List<T> from(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public interface Runner<T> {
        void run(T item, int index);
    }

    // index付forEach
    public static <T> void forEach(@NotNull List<T> list, @NotNull Runner<T> runner) {
        for (int i = 0; i < list.size(); i++) {
            final T item = list.get(i);
            runner.run(item, i);
        }
    }

    public interface Mapper<T, R> {
        R map(T item, int index);
    }

    public static <T, R> List<R> map(@NotNull List<T> list, @NotNull Mapper<T, R> mapper) {
        final List<R> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            final T item = list.get(i);
            ret.add(mapper.map(item, i));
        }
        return ret;
    }

    @NotNull
    public static <T> List<T> reversed(@NotNull List<T> list) {
        final ArrayList<T> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return reversed;
    }

    @Nullable
    public static <T> T find(@NotNull List<T> list, Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    public static <T> int findIndex(@NotNull List<T> list, Predicate<T> predicate) {
        return ListLike.findIndex(list, predicate);
    }

    public static <T> int findLastIndex(@NotNull List<T> list, Predicate<T> predicate) {
        return ListLike.findLastIndex(list, predicate);
    }

    public static <T> boolean removeFirst(@NotNull List<T> list, Predicate<T> predicate) {
        final T target = find(list, predicate);
        return target != null && target == list.remove(list.indexOf(target));
    }

    @Nullable
    public static <T> T replaceFirst(@NotNull List<T> list, Predicate<T> predicate, Function<T, T> supplier) {
        T oldOne = find(list, predicate);
        if (oldOne == null) {
            return null;
        }
        T newOne = supplier.apply(oldOne);
        return list.set(list.indexOf(oldOne), newOne);
    }

    @NotNull
    public static <T> T last(@NotNull List<T> list) {
        return list.get(list.size() - 1);
    }
}