package com.github.guignol.indrah.command;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class SwingWorkerDialog {
    private JFrame owner = null;
    private Dialog dialog = null;

    void init(JFrame owner) {
        if (dialog != null) {
            dialog.dispose();
        }
        // https://docs.oracle.com/javase/jp/6/api/javax/swing/SwingWorker.html#get()
        // http://ateraimemo.com/Swing/BlockingDialog.html
        final JDialog dialog = new JDialog(owner);
        dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
        // 閉じるボタンとか消える
        dialog.setUndecorated(true);
        // 完全に透明だとカーソルも変わらない（下のビューのカーソルになる）
//        final Color color = new Color(0, true);
        final Color color = new Color(0x01000000, true);
//        final Color color = new Color(0x330000F0, true);
        // 先にsetUndecorated(true)しないと落ちる
        dialog.setBackground(color);
        // カーソル
//        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        // Windowにフォーカスが当たったときにリロードするため、
        // この見えないダイアログにフォーカスが当たらないようにする
        dialog.setFocusableWindowState(false);
        this.dialog = dialog;
        this.owner = owner;
    }

    PropertyChangeListener getListener() {
        // ダイアログの変更に備えて現在のダイアログを参照させる
        final Dialog dialogNow = dialog;
        return event -> {
            if (dialogNow == null) {
                return;
            }
            if (event.getPropertyName().equals("state")
                    && event.getNewValue() == SwingWorker.StateValue.DONE) {
                dialogNow.setVisible(false);
//                dialog.dispose();
            }
        };
    }

    void show() {
        if (owner != null) {
            SwingUtilities.invokeLater(() -> {
                // 表示直前に毎回サイズを連動させる
                dialog.setBounds(owner.getBounds());
                dialog.setVisible(true);
            });
        }
    }

    public boolean hasOwner() {
        return owner != null;
    }
}
