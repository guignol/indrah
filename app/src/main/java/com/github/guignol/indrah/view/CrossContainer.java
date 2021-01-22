package com.github.guignol.indrah.view;

import com.github.guignol.indrah.Colors;
import com.github.guignol.indrah.model.Direction;
import com.github.guignol.indrah.model.Directional;
import com.github.guignol.indrah.model.Edge;
import com.github.guignol.swing.rx.SwingScheduler;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CrossContainer {

    public static class Quadrants {

        private final Component upperLeft;
        private final Component upperRight;
        private final Component lowerLeft;
        private final Component lowerRight;

        public Quadrants(@NotNull Component upperLeft,
                         @NotNull Component upperRight,
                         @NotNull Component lowerLeft,
                         @NotNull Component lowerRight) {
            this.upperLeft = upperLeft;
            this.upperRight = upperRight;
            this.lowerLeft = lowerLeft;
            this.lowerRight = lowerRight;
        }
    }

    private static void addHiddenButton(@NotNull final Container container,
                                        @NotNull final Dimension hiddenSize,
                                        @Nullable final ActionListener buttonAction) {
        if (buttonAction != null) {
            final AbstractButton button = ColorfulButton.create(Colors.SEPARATOR_BRIGHT);
            button.setMaximumSize(hiddenSize);
            button.setPreferredSize(hiddenSize);
            button.addActionListener(buttonAction);
            container.add(button);
        }
    }
    public static void init(@NotNull final Container mainContainer,
                            @NotNull final Quadrants quadrant) {
        init(mainContainer, quadrant, new HashMap<>());
    }

    public static void init(@NotNull final Container mainContainer,
                            @NotNull final Quadrants quadrant,
                            @NotNull final Map<Direction, ActionListener>actionMap) {
        // 画面構成
        final ResizableBox fileNameBox;
        final ResizableBox separator;
        final ResizableBox diffBox;
        // 画面全体
        final ResizableBox mainBox;
        // ResizableBox
        {
            // navigatorSizeとautoSealのDimensionを混同しないようにブロックを分ける
            final Dimension navigatorSize = new Dimension(100, 90);
            final Box upperNavigator = Navigator.initAsBox(true, navigatorSize.width);
            fileNameBox = Resizable.layoutHorizontally(upperNavigator);

            final Box lowerNavigator = Navigator.initAsBox(true, navigatorSize.width);
            diffBox = Resizable.layoutHorizontally(lowerNavigator);

            separator = Resizable.layoutHorizontally(navigatorSize.width);
            Navigator.init(separator.parent, false, navigatorSize.height);
            Colors.SEPARATOR.background(separator.head);
            Colors.SEPARATOR.background(separator.tail);
            // 横移動のseparator.navigatorのマウスイベントを親に渡す
            // 親であるseparator.parentは縦移動のnavigator
            separator.navigator.dispatchToParent = true;

            // 横移動を連動させる
            ResizableBox.linkHorizontally(diffBox, fileNameBox, separator);

            // 画面全体
            mainBox = Resizable.layoutVertically(mainContainer,
                    fileNameBox.parent,
                    diffBox.parent,
                    separator.parent);

            // 隠しボタン上
            addHiddenButton(upperNavigator, navigatorSize, actionMap.get(Direction.TOP));
            // 隠しボタン下
            lowerNavigator.add(Box.createVerticalGlue());
            addHiddenButton(lowerNavigator, navigatorSize, actionMap.get(Direction.BOTTOM));
            // 同じ方向のBoxLayoutのネストはダメっぽい
            final FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 0, 0);
            // 隠しボタン左
            separator.head.setLayout(flowLayout);
            separator.head.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            addHiddenButton(separator.head, navigatorSize, actionMap.get(Direction.LEFT));
            // 隠しボタン右
            separator.tail.setLayout(flowLayout);
            separator.tail.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            addHiddenButton(separator.tail, navigatorSize, actionMap.get(Direction.RIGHT));
        }

        // auto seal
        {
            final Dimension autoSeal = new Dimension(100, 100);
            final Directional open = new Directional() {
                @Override
                public void top() {
                    final int bottom = mainBox.parent.getHeight() - (int) (autoSeal.height * 0.75);
                    mainBox.navigator.moveTailSlowly(bottom);
                }

                @Override
                public void bottom() {
                    final int top = (int) (autoSeal.height * 0.75);
                    mainBox.navigator.moveHeadSlowly(top);
                }

                @Override
                public void left() {
                    final int right = mainBox.parent.getWidth() - autoSeal.width;
                    separator.navigator.moveTailSlowly(right);
                }

                @Override
                public void right() {
                    final int left = autoSeal.width;
                    separator.navigator.moveHeadSlowly(left);
                }
            };

            final Sealer upperLeft = new Sealer(fileNameBox.head, quadrant.upperLeft, () -> {
                open.top();
                // 表示するときは横方向も開く
                open.left();
            });
            final Sealer upperRight = new Sealer(fileNameBox.tail, quadrant.upperRight, () -> {
                open.top();
                // 表示するときは横方向も開く
                open.right();
            });
            final Sealer lowerLeft = new Sealer(diffBox.head, quadrant.lowerLeft, () -> {
                open.bottom();
                // 表示するときは横方向も開く
                open.left();
            });
            final Sealer lowerRight = new Sealer(diffBox.tail, quadrant.lowerRight, () -> {
                open.bottom();
                // 表示するときは横方向も開く
                open.right();
            });

            Observable.combineLatest(
                    // 最初は上を詰めるのでTOP, 0
                    mainBox.edgeObservable.startWith(new Edge(Direction.TOP, 0)),
                    Observable.fromArray(fileNameBox, separator, diffBox)
                            .flatMap(box -> box.edgeObservable)
                            // 最初は真ん中のほうにあるので、適当に何も起こらなさそうな値
                            .startWith(new Edge(Direction.RIGHT, autoSeal.width * 4)),
                    Edge.Pair::new)
                    .throttleLast(100, TimeUnit.MILLISECONDS)
                    .observeOn(SwingScheduler.getInstance())
                    .subscribe(pair -> {
                        // 封をするかどうか
                        boolean sealUpperLeft = false;
                        boolean sealUpperRight = false;
                        boolean sealLowerLeft = false;
                        boolean sealLowerRight = false;
                        // 上辺または下辺に近い場合
                        if (pair.vertical.distance < autoSeal.height * 2) {
                            switch (pair.vertical.from) {
                                case TOP:
                                    sealUpperLeft = true;
                                    sealUpperRight = true;
                                    break;
                                case BOTTOM:
                                    sealLowerLeft = true;
                                    sealLowerRight = true;
                                    break;
                            }
                        }
                        // 左辺または右辺に近い場合
                        if (pair.horizontal.distance < autoSeal.width * 2) {
                            switch (pair.horizontal.from) {
                                case LEFT:
                                    sealUpperLeft = true;
                                    sealLowerLeft = true;
                                    break;
                                case RIGHT:
                                    sealUpperRight = true;
                                    sealLowerRight = true;
                                    break;
                            }
                        }
                        upperLeft.seal(sealUpperLeft);
                        upperRight.seal(sealUpperRight);
                        lowerLeft.seal(sealLowerLeft);
                        lowerRight.seal(sealLowerRight);
                    });
        }

        fileNameBox.parent.setMaximumSize(new Dimension(Short.MAX_VALUE, 0));
        fileNameBox.parent.setPreferredSize(new Dimension(Short.MAX_VALUE, 0));

        // 諸々の初期化と画面表示が終わるのを待つ
        // delay==0やinvokeLaterでも動くが、確信が無いので
        final Timer timer = new Timer(150, null);
        timer.addActionListener(e -> {
            timer.stop();
            // TODO 前回の位置（割合）を覚えておく？
            mainBox.navigator.moveHeadSlowly((int) (mainBox.parent.getHeight() * 0.3));
            separator.navigator.moveHeadSlowly((int) (mainBox.parent.getWidth() * 0.6));
        });
        timer.start();
    }
}
