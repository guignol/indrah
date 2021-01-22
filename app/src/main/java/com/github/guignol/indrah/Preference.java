package com.github.guignol.indrah;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class Preference {

    private static final Path userHomeDotAppName = Paths.get(System.getProperty("user.home"), "." + BuildConfig.project);
    private final static Preference shared = new Preference(userHomeDotAppName);

    public static Preference shared() {
        return shared;
    }

    private final Editor editor;

    private Preference(Path where) {
        editor = new Editor(where);
    }

    public Path getDirectory() {
        return editor.where.getParent();
    }

    public void edit(Consumer<PreferenceData> consumer) {
        editor.edit(consumer);
    }

    public PreferenceData readOnly() {
        return editor.readOnly();
    }

    public static class Editor {
        private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        private final String fileName = "preference_v1.json";
        private final Path where;

        @NotNull
        private PreferenceData data;

        public Editor(Path directory) {
            this.where = Paths.get(directory.toAbsolutePath().toString(), fileName);
            this.data = new PreferenceData();
            if (Files.notExists(this.where)) {
                try {
                    Files.createDirectories(this.where.getParent());
                    write(this.where, data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (final BufferedReader reader = Files.newBufferedReader(this.where)) {
                    data = gson.fromJson(reader, PreferenceData.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public PreferenceData readOnly() {
            return gson.fromJson(gson.toJson(this.data), PreferenceData.class);
        }

        public void edit(Consumer<PreferenceData> consumer) {
            consumer.accept(data);
            write(this.where, data);
        }

        private static void write(Path path, PreferenceData data) {
            try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
                //書き出し処理
                final String json = gson.toJson(data);
                writer.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
