package com.github.guignol.indrah.mvvm.arrows;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.model.CommitLogHistory;
import com.github.guignol.indrah.mvvm.figures.Figure;
import com.github.guignol.indrah.mvvm.figures.FiguresModel;
import com.github.guignol.indrah.mvvm.figures.FiguresViewFactory;
import com.github.guignol.indrah.mvvm.log.LogListView;
import com.github.guignol.indrah.utils.ListUtils;
import com.github.guignol.indrah.view.AutoResizeLayer;
import com.github.guignol.swing.binding.Property;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ArrowsLayer {

    public static ArrowsLayer create(FiguresModel model) {
        final Component component = FiguresViewFactory.create(model);
        return new ArrowsLayer(component);
    }

    private final AutoResizeLayer layer;

    private ArrowsLayer(Component drawer) {
        layer = new AutoResizeLayer();

        final Container root = layer.getRoot();
        layer.addPalette(drawer);
        Property.onEvent(root, Property.Event.RESIZED)
                .subscribe(event -> drawer.setSize(root.getSize()));
    }

    public Container getRoot() {
        return layer.getRoot();
    }

    public Container getDefault() {
        return layer.getDefault();
    }

    public List<Figure> getArrows(CommitLogHistory original,
                                  Map<String, Integer> arranged,
                                  VisibleCells beforeVisibleCells,
                                  VisibleCells afterVisibleCells) {
        // 複雑な並び替えはそもそも発生しないと前提すべき
        final List<Figure> arrows = new ArrayList<>();
        if (arranged.isEmpty()) {
            return arrows;
        }
        final Rectangle beforeRect = beforeVisibleCells.convert(layer.getRoot());
        final Rectangle afterRect = afterVisibleCells.convert(layer.getRoot());
        arranged.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .forEach(entry -> {
                    final int beforeIndex = ListUtils.findIndex(original.annotated, an -> an.log.commit.equals(entry.getKey()));
                    if (beforeIndex == -1) {
                        return;
                    }
                    final Integer arrangedIndex = entry.getValue();
                    if (arrangedIndex == null || beforeIndex == arrangedIndex) {
                        // 前後が違うが自分自身のindexが変わってないものは現在そのまま
                        return;
                    }
                    // TODO 色どうしよう
                    final Color color;
                    if (beforeIndex < arrangedIndex) {
                        color = Colors.setAlpha(Color.ORANGE, 200);
                    } else {
                        color = Colors.setAlpha(Color.BLUE, 200);
                    }
                    final Figure figure = getFigure(
                            beforeRect, beforeIndex - beforeVisibleCells.firstIndex,
                            afterRect, arrangedIndex - afterVisibleCells.firstIndex,
                            color
                    );
                    arrows.add(figure);
                });
        return arrows;
    }

    private static Figure getFigure(Rectangle beforeRect, int beforeIndex,
                                    Rectangle afterRect, int arrangedIndex,
                                    Color color) {
        final int cellHeight = LogListView.CellRenderer.CELL_HEIGHT;
        final int margin = LogListView.CellRenderer.LONG_MARGIN;
        final int beforeY = beforeRect.y + cellHeight * beforeIndex + cellHeight / 2;
        final int afterY = afterRect.y + cellHeight * arrangedIndex + cellHeight / 2;
        final Point from = new Point(beforeRect.x + beforeRect.width - margin, beforeY);
        final Point to = new Point(afterRect.x + margin - 5, afterY);
        return new Figure(ArrowPath.toRight(12, from, to), color);
    }

}
