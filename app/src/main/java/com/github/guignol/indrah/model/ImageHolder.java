package com.github.guignol.indrah.model;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ImageHolder {

    public static final ImageHolder DUMMY = new ImageHolder(null);

    @Nullable
    private final Loader<BufferedImage> loader;
    private ImageIcon imageIcon = null;
    private boolean loading = false;
    private Consumer<ImageIcon> notify;

    protected ImageHolder(@Nullable Loader<BufferedImage> loader) {
        this.loader = loader;
    }

    public boolean hasLoader() {
        return loader != null;
    }

    private boolean canLoad() {
        return hasLoader() && imageIcon == null && !loading;
    }

    public ImageIcon lazy(Consumer<ImageIcon> notify) {
        this.notify = notify;
        if (canLoad()) {
            // とりあえず一度だけ
            loading = true;
            assert loader != null;
            loader.load(image -> {
                this.imageIcon = new ImageIcon(image);
                this.notify.accept(this.imageIcon);
            });
        }
        return imageIcon;
    }
}
