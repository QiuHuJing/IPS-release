package dev.efnilite.ip.schematic;

import dev.efnilite.ip.IP;
import dev.efnilite.ip.config.Config;
import dev.efnilite.ip.util.FoliaTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores schematics, so they don't have to be read every time.
 */
public class Schematics {

    private static final String[] SPAWN_SCHEMATICS = new String[]{
            "spawn-island", "spawn-island-duels"
    };
    private static final File FOLDER = IP.getInFolder("schematics");

    /**
     * Reads all files.
     */
    public static void init() {
        FoliaTask.create(IP.getPlugin()).async().execute(() -> {
            if (!FOLDER.exists()) {
                FOLDER.mkdirs();
            }

            File[] files = FOLDER.listFiles((dir, name) -> name.contains("parkour-") || name.contains("spawn-island"));

            if (files == null || files.length == 0) {
                download();
                init();
                return;
            }
          
            try {
                dev.efnilite.vilib.schematic.Schematics.addFromFiles(IP.getPlugin(), files);
            } catch (IOException | ClassNotFoundException ex) {
                IP.logging().stack("Error while trying to load schematics", ex);
            }
        }).run();
    }

    private static void download() {
        IP.log("Downloading schematics");

        List<String> schematics = new ArrayList<>();
        schematics.addAll(Arrays.asList(SPAWN_SCHEMATICS));
        schematics.addAll(Config.SCHEMATICS.getChildren("difficulty", false).stream().map("parkour-%s"::formatted).toList());
        
        for (String file : schematics) {
            String resourcePath = "schematics/" + file;
            try (java.io.InputStream in = IP.getPlugin().getResource(resourcePath)) {
                if (in == null) {
                    IP.logging().error("Schematic resource not found: %s".formatted(resourcePath));
                    continue;
                }
                java.io.File target = IP.getInFolder(resourcePath);
                target.getParentFile().mkdirs();
                java.nio.file.Files.copy(in, target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                IP.log("Extracted schematic: %s".formatted(file));
            } catch (Exception ex) {
                IP.logging().stack("Error while extracting schematic: %s".formatted(resourcePath), ex);
            }
        }
    }
}