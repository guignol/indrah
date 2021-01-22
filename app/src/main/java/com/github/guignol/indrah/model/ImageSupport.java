package com.github.guignol.indrah.model;

import javax.imageio.ImageIO;

public class ImageSupport {
    private static final String[] formats = ImageIO.getReaderFormatNames();

    static boolean checkExtension(String filePath) {
        for (String format : formats) {
            if (filePath.endsWith("." + format)) {
                return true;
            }
        }
        return false;
    }
}
