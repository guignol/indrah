package com.github.guignol.indrah.utils;

import java.util.stream.Stream;

public class StreamUtils {

    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        // Stream.concat()が2つまでなので
        return Stream.of(streams).flatMap(stream -> stream);
    }
}
