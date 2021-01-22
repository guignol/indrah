package com.github.guignol.indrah.mvvm.commit_maker;

import com.github.guignol.indrah.model.Direction;
import com.github.guignol.indrah.model.swing.Draggable;
import com.github.guignol.indrah.mvvm.commit.CommitViewFactory;
import com.github.guignol.indrah.mvvm.diff.DiffListViewFactory;
import com.github.guignol.indrah.mvvm.dragdrop.DragAndDropLayer;
import com.github.guignol.indrah.mvvm.dragdrop.TrackerViewBinder;
import com.github.guignol.indrah.mvvm.filename.NameListViewFactory;
import com.github.guignol.indrah.rx.Animation;
import com.github.guignol.indrah.utils.IndexUtils;
import com.github.guignol.indrah.view.CrossContainer;
import com.github.guignol.swing.binding.Property;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.functions.Consumer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CommitMakerFactory {

    public static Component create(CommitMakerModel model) {
        final DragAndDropLayer layer = new DragAndDropLayer();

        // ファイル名
        final Component workNames = NameListViewFactory.create(model.workspace.nameListModel);
        final Component indexNames = NameListViewFactory.create(model.index.nameListModel);
        // ファイル名のdrag and drop
        TrackerViewBinder.bind(layer, indexNames, model.workspace.trackerModel);
        TrackerViewBinder.bind(layer, workNames, model.index.trackerModel);
        // diff
        final Component workDiff = DiffListViewFactory.create(model.workspace.diffModel);
        final Component indexDiff = DiffListViewFactory.create(model.index.diffModel);

        // コミット
        final Component commitView = CommitViewFactory.create(model.commitAction);
        commitView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                model.commitAction.resize();
            }
        });
        final Function<Dimension, Dimension> relativeSize = dimension -> {
            final int minWidth = 800;
            final int maxWidth = dimension.width;
            final int width = Math.max((int) (maxWidth * 0.8), minWidth);
            final int minHeight = 200;
            final int maxHeight = 300;
            final int height = Math.max((int) (dimension.height * 0.5), minHeight);
            return new Dimension(Math.min(maxWidth, width), Math.min(maxHeight, height));
        };
        final Consumer<Rectangle> resizeCommitView = rectangle -> {
            commitView.setLocation(rectangle.getLocation());
            commitView.setSize(rectangle.getSize());
            commitView.revalidate();
            commitView.repaint();
        };
        model.commitAction.expand = () -> {
            final Dimension rootSize = layer.getRoot().getSize();
            final Dimension size = relativeSize.apply(rootSize);
            final Point destination = commitView.getLocation();
            Animation.resize(commitView.getBounds(), new Rectangle(adjustedLocation(rootSize, size, destination), size))
                    .observeOn(SwingScheduler.getInstance())
                    .subscribe(resizeCommitView);
        };
        model.commitAction.shrink = () -> {
            final Dimension rootSize = layer.getRoot().getSize();
            final Dimension size = new Dimension(100, 100);
            final Point destination = commitView.getLocation();
            // 右下を基準に縮む
            destination.translate(commitView.getWidth(), commitView.getHeight());
            destination.translate(size.width * -1, size.height * -1);
            Animation.resize(commitView.getBounds(), new Rectangle(adjustedLocation(rootSize, size, destination), size))
                    .observeOn(SwingScheduler.getInstance())
                    .subscribe(resizeCommitView);
        };
        // 初期位置（とりあえず枠外に出して、あとは自動で調整される）
        commitView.setLocation(10000, 10000);
        // ドラッグ
        Draggable.with(commitView);
        // ドラッグではみ出した場合
        Property.onEvent(commitView, Property.Event.MOVED)
                .delay(300, TimeUnit.MILLISECONDS)
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .observeOn(SwingScheduler.getInstance())
                .subscribe(event -> {
                    final Dimension rootSize = layer.getRoot().getSize();
                    final Dimension size = commitView.getSize();
                    final Point destination = commitView.getLocation();
                    final Point adjustedLocation = adjustedLocation(rootSize, size, destination);
                    commitView.setLocation(adjustedLocation);
                    commitView.revalidate();
                    commitView.repaint();
                });
        // 親が動いたら最小化
        Property.onEvent(layer.getRoot(), Property.Event.RESIZED).subscribe(event -> {
            // TODO 画面サイズを変更した際に、できれば右下基準で動かしたい
            model.commitAction.shrink();
        });
        layer.addModal(commitView);

        // 画面構成
        final CrossContainer.Quadrants quadrant = new CrossContainer.Quadrants(workNames, indexNames, workDiff, indexDiff);
        CrossContainer.init(layer.getDefault(), quadrant, new HashMap<Direction, ActionListener>() {
            {
                put(Direction.BOTTOM, e -> IndexUtils.undo());
            }
        });

        return layer.getRoot();
    }

    @NotNull
    private static Point adjustedLocation(Dimension rootSize, Dimension targetSize, Point destination) {
        destination.x = Math.min(destination.x, rootSize.width - targetSize.width);
        destination.x = Math.max(destination.x, 0);
        destination.y = Math.min(destination.y, rootSize.height - targetSize.height);
        destination.y = Math.max(destination.y, 0);
        return destination;
    }
}
