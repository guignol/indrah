package com.github.guignol.indrah.model;

import com.github.guignol.indrah.command.DecodeImageHashCommand;
import com.github.guignol.indrah.command.HashObjectCommand;
import com.github.guignol.indrah.command.IndexInfoCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ImageLoader {

    static Loader<BufferedImage> fromHash(@NotNull Path root, @Nullable String hash) {
        if (hash == null) {
            return null;
        }
        // hashから画像を作る
        return new ImageCache(hash, consumer -> new DecodeImageHashCommand(root, hash, consumer).call()).from("Hash");
    }

    static Loader<BufferedImage> fromIndex(@NotNull Path root,
                                           @NotNull String filePath,
                                           @NotNull Map<String, String> hashMap) {
        final String hashCached = hashMap.get(filePath);
        if (hashCached != null) {
            return fromHash(root, hashCached);
        }
        return new ImageCache(consumer -> {
            // indexから画像を作るためにハッシュを取得する
            // 100644 4102bb4f60b8aa307c0fed06c557ca3ce877c1dc 0	xxxx/test.txt
            new IndexInfoCommand(root, filePath)
                    .call(output -> {
                        final String[] fileInfo = output.standardInputs.get(0).split(" ");
                        final String hash1 = fileInfo[1].substring(0, 7);
                        consumer.accept(hash1);
                        hashMap.put(filePath, hash1);
                    });
        }, (hash, consumer) -> new DecodeImageHashCommand(root, hash, consumer).call()).from("Index");
    }

    static Loader<BufferedImage> fromWorkTree(@NotNull Path root,
                                              @NotNull String filePath,
                                              @NotNull Map<String, String> hashMap) {
        final String hashCached = hashMap.get(filePath);
        if (hashCached != null) {
            return fromHash(root, hashCached);
        }
        return new ImageCache(consumer -> {
            // TODO ハッシュはdiffから取れるのでは？
            // ローカルファイルから画像を作るが、キャッシュ制御のためにハッシュを取得する
            new HashObjectCommand(root, filePath).call(output -> {
                final String hash = output.standardInputs.get(0).substring(0, 7);
                consumer.accept(hash);
                hashMap.put(filePath, hash);
            });
        }, consumer -> new Thread(() -> {
            try {
                final Path path = Paths.get(root.toAbsolutePath().toString(), filePath);
                // http://www.ne.jp/asahi/hishidama/home/tech/java/image.html
                final BufferedImage image = ImageIO.read(path.toFile());
                consumer.accept(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start()).from("WorkTree");
    }
}