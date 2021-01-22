package com.github.guignol.indrah.mvvm;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class ValueHolder<T> {

    public static <T> ValueHolder<T> create() {
        return new ValueHolder<>();
    }

    public static <T> ValueHolder<T> createDefault(T defaultValue) {
        return new ValueHolder<>(defaultValue);
    }

    private final BehaviorSubject<T> subject;

    private ValueHolder() {
        this.subject = BehaviorSubject.create();
    }

    private ValueHolder(T defaultValue) {
        this.subject = BehaviorSubject.createDefault(defaultValue);
    }

    public T get() {
        return subject.getValue();
    }

    public void put(T value) {
        subject.onNext(value);
    }

    public Observable<T> observable() {
        return subject.hide();
    }
}
