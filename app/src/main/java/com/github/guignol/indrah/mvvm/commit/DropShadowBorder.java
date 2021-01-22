/*
 * $Id: DropShadowBorder.java,v 1.10 2005/10/13 17:19:34 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

//package org.jdesktop.swingx.border;
package com.github.guignol.indrah.mvvm.commit;

import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

/**
 * https://github.com/atarw/material-ui-swing
 * Implements a DropShadow for components. In general, the DropShadowBorder will
 * work with any rectangular components that do not have a default border installed
 * as part of the look and feel, or otherwise. For example, DropShadowBorder works
 * wonderfully with JPanel, but horribly with JComboBox.
 *
 * @author rbair
 */
public class DropShadowBorder extends AbstractBorder implements Border {
    private enum Position {
        TOP, TOP_LEFT, LEFT, BOTTOM_LEFT,
        BOTTOM, BOTTOM_RIGHT, RIGHT, TOP_RIGHT
    }

    private Color lineColor;
    private int shadowSize;
    private int cornerSize;
    private float shadowOpacity;

    public DropShadowBorder(Color lineColor) {
        this(lineColor, 6);
    }

    public DropShadowBorder(Color lineColor, int shadowSize) {
        this(lineColor, shadowSize, (int) (shadowSize * 1.5), .5f);
    }

    public DropShadowBorder(Color lineColor, int shadowSize, int cornerSize, float shadowOpacity) {
        this.lineColor = lineColor;
        this.shadowSize = shadowSize;
        this.cornerSize = cornerSize;
        this.shadowOpacity = shadowOpacity;
    }

    /**
     * @inheritDoc
     */
    public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
    /*
     * 1) Get images for this border
     * 2) Paint the images for each side of the border that should be painted
     */
        Map<Position, BufferedImage> images = getImages();

        //compute the edges of the component -- not including the border
        //Insets borderInsets = getBorderInsets(c);
        // int leftEdge = x + borderInsets.left - lineWidth;
        // int rightEdge = x + width - borderInsets.right;
        // int topEdge = y + borderInsets.top - lineWidth;
        // int bottomEdge = y + height - borderInsets.bottom;
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(lineColor);

        //The location and size of the shadows depends on which shadows are being
        //drawn. For instance, if the left & bottom shadows are being drawn, then
        //the left shadow extends all the way down to the corner, a corner is drawn,
        //and then the bottom shadow begins at the corner. If, however, only the
        //bottom shadow is drawn, then the bottom-left corner is drawn to the
        //right of the corner, and the bottom shadow is somewhat shorter than before.

        final Point bottomLeftShadowPoint = new Point();
        bottomLeftShadowPoint.setLocation(x + shadowSize, y + height - shadowSize);

        final Point bottomRightShadowPoint = new Point();
        bottomRightShadowPoint.setLocation(x + width - shadowSize, y + height - shadowSize);

        final Point topRightShadowPoint = new Point();
        topRightShadowPoint.setLocation(x + width - shadowSize, y + shadowSize);

        final Rectangle bottomShadowRect = new Rectangle((int) (bottomLeftShadowPoint.getX() + shadowSize), y + height - shadowSize, (int) (bottomRightShadowPoint.getX() - bottomLeftShadowPoint.getX() - shadowSize), shadowSize);
        g2.drawImage(images.get(Position.BOTTOM).getScaledInstance(bottomShadowRect.width, bottomShadowRect.height, Image.SCALE_FAST), bottomShadowRect.x, bottomShadowRect.y, null);

        final Rectangle rightShadowRect = new Rectangle(x + width - shadowSize, (int) (topRightShadowPoint.getY() + shadowSize), shadowSize, (int) (bottomRightShadowPoint.getY() - topRightShadowPoint.getY() - shadowSize));
        g2.drawImage(images.get(Position.RIGHT).getScaledInstance(rightShadowRect.width, rightShadowRect.height, Image.SCALE_FAST), rightShadowRect.x, rightShadowRect.y, null);

        g2.drawImage(images.get(Position.BOTTOM_LEFT), null, (int) bottomLeftShadowPoint.getX(), (int) bottomLeftShadowPoint.getY());
        g2.drawImage(images.get(Position.BOTTOM_RIGHT), null, (int) bottomRightShadowPoint.getX(), (int) bottomRightShadowPoint.getY());
        g2.drawImage(images.get(Position.TOP_RIGHT), null, (int) topRightShadowPoint.getX(), (int) topRightShadowPoint.getY());
    }

    private Map<Position, BufferedImage> getImages() {
        //first, check to see if an image for this size has already been rendered
        //if so, use the cache. Else, draw and save
        Map<Position, BufferedImage> images = new HashMap<>();
      
      /*
       * Do draw a drop shadow, I have to:
       *  1) Create a rounded rectangle
       *  2) Create a BufferedImage to draw the rounded rect in
       *  3) Translate the graphics for the image, so that the rectangle
       *     is centered in the drawn space. The border around the rectangle
       *     needs to be shadowWidth wide, so that there is space for the
       *     shadow to be drawn.
       *  4) Draw the rounded rect as black, with an opacity of 50%
       *  5) Create the BLUR_KERNEL
       *  6) Blur the image
       *  7) copy off the corners, sides, etc into images to be used for
       *     drawing the Border
       */
        int rectWidth = cornerSize + 1;
        RoundRectangle2D rect = new RoundRectangle2D.Double(0, 0, rectWidth, rectWidth, cornerSize, cornerSize);
        int imageWidth = rectWidth + shadowSize * 2;
        BufferedImage image = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
        Graphics2D buffer = (Graphics2D) image.getGraphics();
        buffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        buffer.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        buffer.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        buffer.setColor(new Color(0.0f, 0.0f, 0.0f, shadowOpacity));
        buffer.translate(shadowSize, shadowSize);
        buffer.fill(rect);
        float blurry = 1.0f / (float) (shadowSize * shadowSize);//1.0f / (float)(shadowSize * shadowSize);
        float[] blurKernel = new float[shadowSize * shadowSize];
        for (int i = 0; i < blurKernel.length; i++) {
            blurKernel[i] = blurry;
        }
        ConvolveOp blur = new ConvolveOp(new Kernel(shadowSize, shadowSize, blurKernel));
        BufferedImage targetImage = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
        ((Graphics2D) targetImage.getGraphics()).drawImage(image, blur, -(shadowSize / 2), -(shadowSize / 2));

        int x = 1;
        int y = 1;
        int w = shadowSize;
        int h = shadowSize;
        images.put(Position.TOP_LEFT, targetImage.getSubimage(x, y, w, h));
        x = 1;
        y = h;
        w = shadowSize;
        h = 1;
        images.put(Position.LEFT, targetImage.getSubimage(x, y, w, h));
        x = 1;
        y = rectWidth;
        w = shadowSize;
        h = shadowSize;
        images.put(Position.BOTTOM_LEFT, targetImage.getSubimage(x, y, w, h));
        x = cornerSize + 1;
        y = rectWidth;
        w = 1;
        h = shadowSize;
        images.put(Position.BOTTOM, targetImage.getSubimage(x, y, w, h));
        x = rectWidth;
        y = x;
        w = shadowSize;
        h = shadowSize;
        images.put(Position.BOTTOM_RIGHT, targetImage.getSubimage(x, y, w, h));
        x = rectWidth;
        y = cornerSize + 1;
        w = shadowSize;
        h = 1;
        images.put(Position.RIGHT, targetImage.getSubimage(x, y, w, h));
        x = rectWidth;
        y = 1;
        w = shadowSize;
        h = shadowSize;
        images.put(Position.TOP_RIGHT, targetImage.getSubimage(x, y, w, h));
        x = shadowSize;
        y = 1;
        w = 1;
        h = shadowSize;
        images.put(Position.TOP, targetImage.getSubimage(x, y, w, h));

        buffer.dispose();
        image.flush();

        return images;
    }

    /**
     * @inheritDoc
     */
    public Insets getBorderInsets(Component c) {
        final int top = 0;
        final int left = 0;
        final int bottom = shadowSize;
        final int right = shadowSize;
        return new Insets(top, left, bottom, right);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isBorderOpaque() {
        return true;
    }
}