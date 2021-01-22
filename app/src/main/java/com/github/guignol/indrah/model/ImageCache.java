package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

// キャッシュライブラリ諸々
// http://d.hatena.ne.jp/Kazuhira/20130723/1374587549
// ただ、基本的に細かい制御はいらないので自前で
// TODO 明示的な破棄をどこで行うか
public class ImageCache implements Loader<BufferedImage> {

    private static final CacheRepository<String, BufferedImage> cacheRepository = new CacheRepository<>();

    @NotNull
    private final Loader<String> hashLoader;
    @NotNull
    private final Loader2<String, BufferedImage> otherwise;

    private String from = "no where";

    public ImageCache(@NotNull String hash, @NotNull Loader<BufferedImage> otherwise) {
        this(consumer -> consumer.accept(hash), otherwise);
    }

    public ImageCache(@NotNull Loader<String> hashLoader, @NotNull Loader<BufferedImage> otherwise) {
        this(hashLoader, (trigger, consumer) -> otherwise.load(consumer));
    }

    public ImageCache(@NotNull Loader<String> hashLoader, @NotNull Loader2<String, BufferedImage> otherwise) {
        this.hashLoader = hashLoader;
        this.otherwise = otherwise;
    }

    public ImageCache from(String from) {
        this.from = from;
        return this;
    }

    @Override
    public void load(@NotNull Consumer<BufferedImage> consumer) {
        this.hashLoader.load(key -> cacheRepository.load(key, consumer, otherwise, from));
    }
}
