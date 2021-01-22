package com.github.guignol.indrah.mvvm;

import com.github.guignol.indrah.Preference;
import com.github.guignol.indrah.command.Command;
import com.github.guignol.indrah.mvvm.common.Directory;
import com.github.guignol.indrah.mvvm.common.HistoryControl;
import com.github.guignol.indrah.mvvm.common.WindowSwitcher;
import com.github.guignol.indrah.mvvm.main_window.MainWindowFactory;
import com.github.guignol.indrah.mvvm.main_window.MainWindowModel;
import com.github.guignol.indrah.mvvm.sidebar.SideBarModel;
import com.github.guignol.indrah.mvvm.sidebar.SideBarViewFactory;
import com.github.guignol.indrah.utils.FileUtils;
import com.github.guignol.swing.rx.SwingScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Application {

    private final Preference preference;

    private final Directory directory;
    private final SideBarModel sideBarModel;
    private final Component sideBarComponent;
    private MainWindowModel windowModel;

    // 履歴
    private final HistoryControl historyControl;

    private final JFrame frame = new JFrame("App");

    public Application(@NotNull Preference preference) {
        this.preference = preference;
        this.historyControl = new HistoryControl(preference);
        this.directory = new Directory(historyControl);
        this.directory.onSelectionNeeded()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(eventStatus -> FileUtils.selectDirectory(frame).subscribe((path, throwable) -> {
                    if (throwable == null) {
                        this.directory.put(path);
                    }
                }));
        this.directory.onChange()
                .observeOn(SwingScheduler.getInstance())
                .subscribe(this::updateDirectory);
        this.sideBarModel = new SideBarModel(this.directory, new WindowSwitcher());
        this.sideBarComponent = SideBarViewFactory.create(this.sideBarModel);

        final URL resource = getClass().getClassLoader().getResource("icon.png");
        frame.setIconImage(Toolkit.getDefaultToolkit().createImage(resource));

        Command.initDialog(frame);

        // メニューについての参考ページ
        // http://zetcode.com/tutorials/javaswingtutorial/menusandtoolbars/
        {
            // メニューバー
            final JMenuBar menuBar = new JMenuBar();
            {
                // File
                final JMenu file = new JMenu("File");
                {
                    // Open
                    {
                        final JMenu open = new JMenu("Open");
                        {
                            // Choose
                            final JMenuItem choose = new JMenuItem("Choose...");
                            choose.addActionListener(event -> this.directory.select());
                            open.add(choose);
                        }

                        // 区切り線
                        final JPopupMenu.Separator separator = new JPopupMenu.Separator();
                        historyControl.updated()
                                .subscribe(updated -> {
                                    // 区切り線を削除
                                    open.remove(separator);
                                    // サブメニューから履歴を削除
                                    final JMenuItem chooser = open.getItem(0);
                                    open.removeAll();
                                    open.add(chooser);

                                    if (updated.isEmpty()) {
                                        // 追加する履歴なし
                                        return;
                                    }
                                    // 区切り線を追加
                                    open.add(separator);
                                    // 改めて履歴を追加
                                    updated.forEach(path -> {
                                        final JMenuItem menuItem = new JMenuItem(path.toAbsolutePath().toString());
                                        menuItem.addActionListener(e -> start(path));
                                        open.add(menuItem);
                                    });
                                });
                        file.add(open);
                    }
                    // reboot
                    {
                        final JMenuItem reboot = new JMenuItem("Reboot");
                        reboot.addActionListener(event -> init());
                        file.add(reboot);
                    }
                    // Exit
                    {
                        final JMenuItem exit = new JMenuItem("Exit");
                        exit.addActionListener(event -> System.exit(0));
                        file.add(exit);
                    }
                }
                menuBar.add(file);
            }
            {
                // Browse
                final JMenu browse = new JMenu("Browse");
                {
                    // Explorer
                    {
                        final JMenuItem explorer = new JMenuItem("Explorer");
                        explorer.addActionListener(event -> this.directory.openExplorer());
                        browse.add(explorer);
                    }
                }
                menuBar.add(browse);
            }
            frame.setJMenuBar(menuBar);
        }

        init();

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 500));
        // 最大化（ディスプレイが大きいと邪魔かも）
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.pack();
        frame.setVisible(true);
    }

    private void init() {
        final Path lastPath = windowModel == null ? null : this.directory.get();
        final Container contentPane = frame.getContentPane();
        if (contentPane.getComponentCount() != 0) {
            contentPane.removeAll();
        }
        // windowModelを初期化
        // メモリ使用量の変化を見る限りリークはしてなさそう
        windowModel = new MainWindowModel(this.preference, this.sideBarModel);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.LINE_AXIS));
        contentPane.add(MainWindowFactory.create(windowModel));
        contentPane.add(this.sideBarComponent);

        // on reboot
        if (lastPath != null) {
            start(lastPath);
        }
    }

    private void updateDirectory(Path path) {
        // タイトル更新
        final String title = displayPath(path);
        frame.setTitle(title);
        // 選択履歴を更新
        historyControl.add(path);

        windowModel.reload();
    }

    public void start() {
        final Path lastRepository = historyControl.lastRepository();
        if (lastRepository != null) {
            start(lastRepository);
            return;
        }
        this.directory.select();
    }

    private void start(@Nullable final Path path) {
        if (path == null || !Files.exists(path)) {
            if (this.directory.get() == null) {
                this.directory.select();
            }
            return;
        }
        this.directory.put(path);
    }

    private static String displayPath(Path directory) {
        return directory.getFileName().toString() + " - [" + directory.toString() + "]";
    }
}
