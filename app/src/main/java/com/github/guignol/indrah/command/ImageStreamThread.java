package com.github.guignol.indrah.command;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageStreamThread extends Thread {

    public static ImageStreamThread startWith(InputStream is) {
        final ImageStreamThread thread = new ImageStreamThread(is);
        thread.start();
        return thread;
    }

    private final InputStream is;
    private BufferedImage image = null;


    public ImageStreamThread(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        try {
            image = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    BufferedImage get() throws InterruptedException {
        // スレッド終了待ち
        join();
        return image;
    }
}
