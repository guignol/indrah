package com.github.guignol.indrah.utils;

public class ArrayUtils {
    @SafeVarargs
    public static <T> T[] of(T... args) {
        return args;
    }

    public static <T> boolean isBlank(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isBlank(int[] array) {
        return array == null || array.length == 0;
    }
}
