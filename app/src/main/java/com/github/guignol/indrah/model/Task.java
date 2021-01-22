package com.github.guignol.indrah.model;

import com.github.guignol.indrah.utils.ListUtils;

import java.util.List;

public interface Task<T> {

    void run(Resolver<T> resolver);

    @SuppressWarnings("SameReturnValue")
    static <T> T ABORT() {
        return null;
    }

    static <T> void chain(List<Task<T>> tasks) {
        Task.chain(null, tasks);
    }

    @SafeVarargs
    static <T> void chain(T first, List<Task<T>> tasks, Task<T>... rest) {
        if (rest != null && 0 < rest.length) {
            tasks.addAll(ListUtils.from(rest));
        }
        callNext(first, tasks);
    }

    @SafeVarargs
    static <T> void chain(Task<T>... tasks) {
        Task.chain(null, tasks);
    }

    @SafeVarargs
    static <T> void chain(T first, Task<T>... tasks) {
        Task.chain(first, ListUtils.from(tasks));
    }

    static <T> void callNext(T input, List<Task<T>> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        final Task<T> task = tasks.get(0);
        task.run(new Resolver<T>(input) {
            @Override
            public void resolve(T result) {
                // abort when no output
                if (result == ABORT()) {
                    return;
                }
                tasks.remove(task);
                callNext(result, tasks);
            }
        });
    }

    abstract class Resolver<T> {
        public final T input;

        public Resolver() {
            this(null);
        }

        public Resolver(T input) {
            this.input = input;
        }

        public abstract void resolve(T result);
    }
}
