package com.github.guignol.indrah.mvvm.arrows;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class ArrowPath {

    public static Path2D toRight(int size, Point from, Point to) {
        final int arrowHeight = (int) (size * 1.0);
        final int arrowWing = (int) (size * 1.0);
        final Path2D path = new Path2D.Double();
        // 矢印の先端を先に描く
        path.moveTo(to.x, to.y);
        final int arrowStartX = to.x - arrowHeight;
        path.lineTo(arrowStartX, to.y - arrowWing);
        path.lineTo(arrowStartX, to.y + arrowWing);
        path.closePath();

        // 矢印の先端から線を左に水平に伸ばす
        final int cornerOffsetX = (to.x - from.x) / 2;
        final Point junction = new Point(to.x - cornerOffsetX, to.y - size / 2);
        path.append(new Rectangle(junction, new Dimension(cornerOffsetX - arrowHeight, size)), false);

        // さらにfromまでの残りの距離分の線を左に水平に伸ばす
        final Point junctionCenter = new Point(junction.x, junction.y + size / 2);
        final int distance = getDistance(from, junctionCenter);
        final Rectangle horizontal = new Rectangle(
                new Point(junction.x - distance, junction.y),
                new Dimension(distance, size));
        // 隙間ができないように然るべきアンカーでfromまで回転させる
        final Point anchor;
        if (from.y < to.y) {
            // 右下がり
            anchor = new Point(junction.x, junction.y + size);
        } else {
            // 右上がり
            anchor = junction;
        }
        final AffineTransform transform = new AffineTransform();
        transform.rotate(getRadian(from, junctionCenter), anchor.x, anchor.y);
        path.append(transform.createTransformedShape(horizontal), false);

        return path;
    }

    public static Path2D get(int size, Point from, Point to) {
        final Path2D path = new Path2D.Double();
        final int x = (int) (from.x - size * 0.5);
        final int height = getDistance(from, to);
        Rectangle rect2 = new Rectangle(x, from.y, size, height);
        path.append(rect2, false);
        final int bottomY = from.y + height;
        path.moveTo(x, bottomY);
        path.lineTo(from.x - size * 0.8, bottomY);
        path.lineTo(from.x, bottomY + size * 0.8);
        path.lineTo(from.x + size * 0.8, bottomY);
        path.lineTo(from.x + size * 0.5, bottomY);

        final AffineTransform transform = new AffineTransform();
        transform.rotate(getRadian(from, to), from.x, from.y);
        transform.quadrantRotate(3, from.x, from.y);
        path.transform(transform);

        return path;
    }

    private static int getDistance(Point from, Point to) {
        return (int) Math.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2));
    }

    private static double getRadian(Point from, Point to) {
        return Math.atan2(to.y - from.y, to.x - from.x);
    }
}
