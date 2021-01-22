package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.model.CommitLog;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.utils.StringUtils;
import com.github.guignol.swing.binding.IView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

class RewriteMessageDialog implements IView<RewriteMessageDialogViewModel> {

    @Override
    public void bind(RewriteMessageDialogViewModel viewModel) {
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                viewModel.close();
            }
        });

        viewModel.onMessageEdit()
                .subscribe(this::show);
    }

    private final JDialog dialog;
    private final MessageEditor editor;

    RewriteMessageDialog(JFrame frame) {
        // TODO ドラッグ可能にする
        // TODO 色等をコミットに合わせる
        dialog = new JDialog(frame);
        dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
//        dialog.setUndecorated(true);
        final Rectangle bounds = new Rectangle(100, 100, 500, 300);
        dialog.setBounds(bounds);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        editor = new MessageEditor();
        dialog.getContentPane().add(editor.getComponent(), BorderLayout.CENTER);
        JButton done = new JButton("done");
        done.addActionListener(e -> {
            editor.save();
            dialog.setVisible(false);
        });
        dialog.getContentPane().add(done, BorderLayout.SOUTH);
    }

    private void show(CommitLog log) {
        editor.start(log);
        dialog.setVisible(true);
    }

    private class MessageEditor implements ComponentHolder {
        private final JTextArea input = new JTextArea();
        private CommitLog log;

        void start(CommitLog log) {
            this.log = log;
            final String newMessage = log.getNewMessage();
            if (StringUtils.isBlank(newMessage)) {
                input.setText(log.message);
            } else {
                input.setText(newMessage);
            }
        }

        void save() {
            log.editMessage(input.getText());
        }

        @Override
        public Component getComponent() {
            return input;
        }
    }
}
