package com.github.guignol.indrah.mvvm.figures;

import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.processor.View;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@View(factoryName = "FiguresViewFactory")
class FiguresDrawer extends JComponent implements IView<FiguresViewModel> {

    @Override
    public void bind(FiguresViewModel viewModel) {
        viewModel.onNext().subscribe(this::add);
    }

    private final List<Figure> figures;

    FiguresDrawer() {
        setOpaque(false);
        figures = new ArrayList<>();
    }

    private void add(Figure figure) {
        if (figure.path == null && figure.color == null) {
            figures.clear();
            draw();
            return;
        }
        figures.add(figure);
        draw();
    }

    private void draw() {
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        figures.forEach(figure -> {
            g2.setColor(figure.color);
            g2.fill(figure.path);
        });
        g2.dispose();
    }
}
