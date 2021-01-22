package com.github.guignol.indrah.rx;

import io.reactivex.schedulers.Timed;

import java.util.Objects;

public class Times {
    public static <T> Timed<T> timed(Timed from, T value) {
        return new Timed<>(value, from.time(), from.unit());
    }

    public static <T> boolean sameTime(Timed<T> one, Timed<T> another) {
        return one.time() == another.time() && Objects.equals(one.unit(), another.unit());
    }
}
