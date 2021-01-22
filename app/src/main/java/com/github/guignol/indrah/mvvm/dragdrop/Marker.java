package com.github.guignol.indrah.mvvm.dragdrop;

import javax.swing.*;
import java.util.List;

public interface Marker {
    void hide();
    List<JLabel> get(int count);
}
