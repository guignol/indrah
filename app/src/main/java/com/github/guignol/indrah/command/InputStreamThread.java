package com.github.guignol.indrah.command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * InputStreamを読み込むスレッド
 * http://www.ne.jp/asahi/hishidama/home/tech/java/process.html#h4_std_thread
 */
public class InputStreamThread extends Thread {

    public static final String UTF_8 = StandardCharsets.UTF_8.toString();

    public static InputStreamThread startWith(final InputStream is) {
        final InputStreamThread thread = new InputStreamThread(is, UTF_8);
        thread.start();
        return thread;
    }

    public static InputStreamThread startWith(final InputStream is, final String charset) {
        final InputStreamThread thread = new InputStreamThread(is, charset);
        thread.start();
        return thread;
    }

    private final BufferedReader br;

    private final List<String> list = new ArrayList<>();

    public InputStreamThread(final InputStream is, final String charset) {
        try {
            br = new BufferedReader(new InputStreamReader(is, charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                String line = br.readLine();
                if (line == null) break;
                list.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> get() throws InterruptedException {
        // スレッド終了待ち
        join();
        return list;
    }
}
