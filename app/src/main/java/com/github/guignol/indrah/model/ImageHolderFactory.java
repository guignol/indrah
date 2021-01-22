package com.github.guignol.indrah.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;

public class ImageHolderFactory {

    public static ImageHolder get(@Nullable Path root, Diff diff, boolean cached) {
        if (root == null) {
            return ImageHolder.DUMMY;
        }
        // 拡張子チェック
        // TODO diff.names.any()なので雑
        if (!ImageSupport.checkExtension(diff.summary.names.any())) {
            return ImageHolder.DUMMY;
        }

        if (cached) {
            // -----------------index
            // 基本的に、ワークスペースから削除・変更されている可能性があるのでindexを使う
            // 新規追加 →index
            // 削除 → index
            // 修正 → indexとindex
            // リネームのみ → index
            // TODO リネーム修正 → 画像でそういうのがあるのか不明。後回し
            switch (diff.summary.status) {
                case Added:
                    return fromHash(root, DiffHeader.hashAfter(diff.headerLines()));
                case Deleted:
                    // 削除されてstageしたらindexに存在しない
                    return fromHash(root, DiffHeader.hashBefore(diff.headerLines()));
                case Modified:
                    final Loader<BufferedImage> beforeLoader = ImageLoader.fromHash(root, DiffHeader.hashBefore(diff.headerLines()));
                    final Loader<BufferedImage> afterLoader = ImageLoader.fromHash(root, DiffHeader.hashAfter(diff.headerLines()));
                    return compare(beforeLoader, afterLoader);
                case Renamed:
                    // 100%のリネームだとハッシュが変化しないのでヘッダーからは取得できない
                    return fromIndex(root, diff.summary.names.after(), diff.hashMap);
            }
        } else {
            // -----------------ワークスペース
            // 新規追加 → names.after()
            // 削除 → index
            // 修正 → indexとtarget.after()
            // リネームのみ → names.after()
            // TODO リネーム修正 → 画像でそういうのがあるのか不明。後回し
            switch (diff.summary.status) {
                case Added:
                    return fromWorkTree(root, diff.summary.names.after(), diff.hashMap);
                case Deleted:
                    return fromHash(root, DiffHeader.hashBefore(diff.headerLines()));
                case Modified:
                    final Loader<BufferedImage> beforeLoader = ImageLoader.fromHash(root, DiffHeader.hashBefore(diff.headerLines()));
                    final Loader<BufferedImage> afterLoader = ImageLoader.fromWorkTree(root, diff.summary.names.after(), diff.hashMap);
                    return compare(beforeLoader, afterLoader);
                case Renamed:
                    // TODO 今のところ、このパターンは無い（削除と、untrackedな追加に分かれる）
                    return fromWorkTree(root, diff.summary.names.after(), diff.hashMap);
            }
        }

        return ImageHolder.DUMMY;
    }

    public static ImageHolder fromHash(@NotNull Path root, @Nullable String hash) {
        return new ImageHolder(ImageLoader.fromHash(root, hash));
    }

    public static ImageHolder fromIndex(@NotNull Path root, @NotNull String filePath, Map<String, String> hashMap) {
        return new ImageHolder(ImageLoader.fromIndex(root, filePath, hashMap));
    }

    public static ImageHolder fromWorkTree(@NotNull Path root, @NotNull String filePath, Map<String, String> hashMap) {
        return new ImageHolder(ImageLoader.fromWorkTree(root, filePath, hashMap));
    }

    public static ImageHolder compare(Loader<BufferedImage> before, Loader<BufferedImage> after) {
        return new ImageHolder(consumer -> before.load(beforeImage -> after.load(afterImage -> {
            final BufferedImage image = new ImageComposer(beforeImage, afterImage).build();
            consumer.accept(image);
        })));
    }
}
