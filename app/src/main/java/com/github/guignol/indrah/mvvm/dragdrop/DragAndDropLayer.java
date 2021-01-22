package com.github.guignol.indrah.mvvm.dragdrop;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.view.AutoResizeLayer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DragAndDropLayer {

    public static JLabel createTrackMarker() {
        return new JLabel() {
            {
                setOpaque(true);
                setBackground(Colors.SELECTED_FILE_NAME.get());
            }
        };
    }

    private final AutoResizeLayer layer;
    public final JLabel targetMarker;
    public final JLabel trackMarker;
    public final Marker subMarker;

    public DragAndDropLayer() {
        layer = new AutoResizeLayer();

        targetMarker = new JLabel() {
            {
                setOpaque(true);
                setBackground(new Color(0x88000000, true));
            }
        };
        layer.addPalette(targetMarker);
        trackMarker = createTrackMarker();
        // TODO スクロールバーの裏を通っている
        layer.addPalette(trackMarker);

        subMarker = new SubMarker(layer);
    }

    public Container getRoot() {
        return layer.getRoot();
    }

    public Container getDefault() {
        return layer.getDefault();
    }

    public void addModal(Component modal) {
        layer.addModal(modal);
    }

    public void addModal(Component modal, Function<Dimension, Dimension> relativeSize) {
        layer.addModal(modal, relativeSize);
    }

    private static class SubMarker implements Marker {

        private final AutoResizeLayer layer;
        private final List<JLabel> subMarker = new ArrayList<>();

        SubMarker(AutoResizeLayer layer) {
            this.layer = layer;
        }

        @Override
        public void hide() {
            subMarker.forEach(label -> label.setVisible(false));
        }

        @Override
        public List<JLabel> get(int count) {
            if (count == 0) {
                remove(subMarker.size());
                return new ArrayList<>();
            } else {
                final int offset = subMarker.size() - count;
                if (0 <= offset) {
                    remove(offset);
                } else {
                    for (int i = 0; i < Math.abs(offset); i++) {
                        final JLabel added = createTrackMarker();
                        layer.addPalette(added);
                        subMarker.add(added);
                    }
                }
                return new ArrayList<>(subMarker);
            }
        }

        private void remove(int count) {
            for (int i = 0; i < count; i++) {
                final JLabel removed = subMarker.remove(0);
                layer.remove(removed);
            }
        }
    }
}
