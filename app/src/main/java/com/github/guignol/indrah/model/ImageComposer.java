package com.github.guignol.indrah.model;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageComposer {
    private final BufferedImage before;
    private final BufferedImage after;

    public ImageComposer(BufferedImage before, BufferedImage after) {
        this.before = before;
        this.after = after;
    }

    // https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q11102248301
    public BufferedImage build() {
        // 合成後の画像の縦横サイズを指定して、新しい画像オブジェクトを作る
        // この時点では画像は真っ黒な状態
        final BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        // その真っ黒画像に描画するためのGraphicsオブジェクトを取り出して、描画する
        final Graphics g = img.getGraphics();
        g.drawImage(before, 0, 0, null);
        g.drawImage(after, 0, after.getHeight(), null);
        return img;
    }

    private int getWidth() {
        return Math.max(before.getWidth(), after.getWidth());
    }

    private int getHeight() {
        return before.getHeight() + after.getHeight();
    }
}
