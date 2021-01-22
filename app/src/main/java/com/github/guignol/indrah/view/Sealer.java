package com.github.guignol.indrah.view;

import com.github.guignol.indrah.Colors;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class Sealer {

    private static final String CARD_MAIN = "main";
    private static final String CARD_SEAL = "seal";

    private final Container container;
    private final CardLayout cardLayout;

    public Sealer(@NotNull final Container container,
                  @NotNull final Component component,
                  @NotNull final Runnable onSealClicked) {
        this.container = container;

        cardLayout = new CardLayout();
        container.setLayout(cardLayout);
        container.add(new JButton() {
            {
                Colors.HEAVY.background(this);
                addActionListener(e -> onSealClicked.run());
            }
        }, CARD_SEAL);
        container.add(component, CARD_MAIN);
    }

    public void seal(boolean seal) {
        if (seal) {
            cardLayout.show(container, CARD_SEAL);
        } else {
            cardLayout.show(container, CARD_MAIN);
        }
    }
}
