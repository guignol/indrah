package com.github.guignol.indrah.mvvm.commit;

import com.github.guignol.indrah.Colors;
import com.github.guignol.swing.binding.ComponentHolder;
import com.github.guignol.indrah.utils.StringUtils;
import com.github.guignol.indrah.view.ColorfulButton;
import com.github.guignol.swing.binding.Bindable;
import com.github.guignol.swing.binding.IView;
import com.github.guignol.swing.processor.View;
import io.reactivex.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import static com.github.guignol.indrah.mvvm.commit.CommitViewModel.*;

@View
public class CommitView implements ComponentHolder, IView<CommitViewModel> {

    private final JPanel inner;
    private final JTextArea inputMessage;
    private final JLabel userInfo;
    private final AbstractButton commitButton;
    private final JCheckBox allowEmpty;
    private final JCheckBox amend;
    private final JCheckBox noEdit;
    private final JPanel outer;

    @Override
    public void bind(CommitViewModel viewModel) {
        Bindable.view(inputMessage)
                .toViewModel(viewModel.inputMessage);
        Bindable.view(commitButton)
                .toViewModel(viewModel.commitButtonAction);
        Bindable.view(allowEmpty, AbstractButton::isSelected)
                .toViewModel(viewModel.allowEmpty);
        Bindable.view(noEdit, AbstractButton::isSelected)
                .toViewModel(viewModel.noEdit);
        Bindable.view(amend, AbstractButton::isSelected)
                .toViewModel(viewModel.amend);

        // HEADのコミットメッセージ
        viewModel.bindText(TEXT.COMMIT_MESSAGE)
                .toView(inputMessage::setText);
        // user.nameとuser.email
        viewModel.onGitUser()
                .subscribe(this::showUser);
        // コミットボタンの表示
        viewModel.bindText(TEXT.COMMIT_BUTTON)
                .toView(commitButton::setText);
        // enabled/disabledの更新
        viewModel.bindEnabled(ENABLED.INPUT_MESSAGE)
                .toView(inputMessage::setEnabled);
        viewModel.bindEnabled(ENABLED.ALLOW_EMPTY)
                .toView(allowEmpty::setEnabled);
        viewModel.bindEnabled(ENABLED.AMEND)
                .toView(amend::setEnabled);
        viewModel.bindEnabled(ENABLED.NO_EDIT)
                .toView(noEdit::setEnabled);
        // コミット後
        viewModel.bindEvent(EVENT.COMMIT)
                .toView(eventStatus -> {
                    inputMessage.setText("");
                    allowEmpty.setSelected(false);
                    amend.setSelected(false);
                    noEdit.setSelected(false);
                });
        // 開閉時（Ctrl + Enterと文字入力ができるようにしたり、できないようにしたり）
        viewModel.bindEvent(EVENT.EXPAND)
                .toView(eventStatus -> inputMessage.requestFocus());
        viewModel.bindEvent(EVENT.SHRINK)
                .toView(eventStatus -> inner.requestFocus());
    }

    private static final float fontSize = 20.f;
    private static final int marginOrPadding = 20;

    CommitView() {
        final Colors.FlowColor panelColor = Colors.POPUP_BACK;
        final Colors.FlowColor inputColorEnabled = Colors.DARK_THEME.brighter(0.8);
        final Colors.FlowColor inputColorDisabled = panelColor.darker(0.95);

        inner = new JPanel();
        inner.setOpaque(true);
        panelColor.background(inner);
        inner.setBorder(BorderFactory.createEmptyBorder(marginOrPadding, marginOrPadding, marginOrPadding, marginOrPadding));

        // user.name user.email
        userInfo = new JLabel() {
            {
                Colors.HEAVY.foreground(this);
            }
        };

        // コミットするボタン
        commitButton = ColorfulButton.create(Colors.POPUP_BUTTON);
        final Color commitButtonTextColor = Colors.darker(Color.WHITE, 0.9);
        commitButton.setForeground(commitButtonTextColor);
        commitButton.setFont(commitButton.getFont().deriveFont(fontSize));

        // コミットメッセージ入力
        inputMessage = new JTextArea() {

            private Disposable disposable;

            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                if (disposable != null) {
                    disposable.dispose();
                    disposable = null;
                }
                if (enabled) {
                    disposable = inputColorEnabled.background(this);
                } else {
                    disposable = inputColorDisabled.background(this);
                }
            }
        };
        inputMessage.setFont(inputMessage.getFont().deriveFont(fontSize));
        inputMessage.setEnabled(true);
        inputMessage.setForeground(Color.WHITE);
        inputMessage.setCaretColor(Color.WHITE);
        inputMessage.putClientProperty("caretWidth", 6);
        inputMessage.setMargin(new Insets(marginOrPadding, marginOrPadding, marginOrPadding, marginOrPadding));
        // macのcommandはMETA_MASKらしいけど、試せていない
        // また、macのreturnもどうなるか未確認
//        final int modifier = InputEvent.CTRL_MASK;
//        final int modifier = InputEvent.META_MASK;
        final int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        // ctrl + enter または command + enter でコミット
        final KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, modifier);
        inputMessage.getKeymap()
                .addActionForKeyStroke(ctrlEnter, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        commitButton.doClick();
                    }
                });
        inner.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                inputMessage.requestFocus();
            }
        });

        // コミットオプション
        allowEmpty = createCheckBox("--allow-empty");
        amend = createCheckBox("--amend");
        noEdit = createCheckBox("--amend --no-edit");

        // レイアウト
        inner.setLayout(new BoxLayout(inner, BoxLayout.LINE_AXIS));
        inner.add(new JPanel() {
            {
                final Dimension size1 = new Dimension(marginOrPadding * 4, Short.MAX_VALUE);
                setMaximumSize(size1);
                setMinimumSize(size1);
                setOpaque(true);
                setBackground(commitButtonTextColor);
            }
        });
        inner.add(Box.createHorizontalStrut(marginOrPadding));
        inner.add(new JScrollPane(inputMessage) {
            {
                setBorder(BorderFactory.createEmptyBorder());
                final Dimension size = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
                setMaximumSize(size);
                setPreferredSize(size);
            }
        });
        inner.add(Box.createHorizontalStrut(marginOrPadding));

        final JPanel controlPanel = new JPanel();
        {
            final Dimension size = new Dimension(320, Short.MAX_VALUE);
            controlPanel.setMaximumSize(size);
            controlPanel.setMinimumSize(size);
            controlPanel.setPreferredSize(size);
            controlPanel.setOpaque(false);

            final Dimension commitButtonSize = new Dimension(Short.MAX_VALUE, 100);
            commitButton.setMaximumSize(commitButtonSize);
            commitButton.setPreferredSize(commitButtonSize);

            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
            controlPanel.add(userInfo);
            controlPanel.add(Box.createVerticalStrut(marginOrPadding / 2));
            controlPanel.add(commitButton);
            controlPanel.add(Box.createVerticalGlue());
            controlPanel.add(allowEmpty);
            controlPanel.add(amend);
            controlPanel.add(noEdit);
        }
        inner.add(controlPanel);


        outer = new JPanel();
        outer.setLayout(new CardLayout());
        outer.setOpaque(false);
        outer.add(inner);
        outer.setBorder(new DropShadowBorder(Color.BLACK, 6));
    }

    @Override
    public Component getComponent() {
        return outer;
    }

    private static JCheckBox createCheckBox(final String title) {
        final JCheckBox checkBox = new JCheckBox(title);
        checkBox.setFont(checkBox.getFont().deriveFont(20.f));
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setSelected(false);
        checkBox.setForeground(Color.WHITE);
        return checkBox;
    }

    private void showUser(GitUser user) {
        userInfo.setText(Stream.of(
                user.name,
                user.email
        ).collect(StringUtils.toHtml));
    }
}
