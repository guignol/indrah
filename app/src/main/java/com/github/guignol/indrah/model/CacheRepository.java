package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CacheRepository<KEY, VALUE> {
    private final Map<KEY, SoftReference<VALUE>> cache = new HashMap<>();
    private final Map<KEY, Loader2<KEY, VALUE>> loading = new WeakHashMap<>();
    private final MultiMap<KEY, Consumer<VALUE>> waiting = new MultiMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    public void load(@NotNull KEY key,
                     @NotNull Consumer<VALUE> consumer,
                     @NotNull Loader2<KEY, VALUE> otherwise,
                     @NotNull String from) {
        executor.execute(() -> {
            final SoftReference<VALUE> reference = cache.get(key);
            final VALUE data;
            if (reference != null && (data = reference.get()) != null) {
                System.out.println(key + " : use image cached □□□ from " + from);
                // キャッシュあり
                consumer.accept(data);
            } else {
                if (this.loading.get(key) == null) {
                    System.out.println(key + " : use image created ■■■ from " + from);
                    //データを取得
                    this.loading.put(key, otherwise);
                    otherwise.load(key, saveFor(key).andThen(consumer));
                } else {
                    System.out.println(key + " : waiting image created ... from " + from);
                    // 同じキーのデータ取得中なので待つ
                    waiting.put(key, consumer);
                }
            }
        });
    }

    private Consumer<VALUE> saveFor(KEY key) {
        return value -> executor.execute(() -> {
            cache.put(key, new SoftReference<>(value));
            waiting.forEach(key, waiter -> waiter.accept(value));
            waiting.clear(key);
            loading.remove(key);
        });
    }
}
