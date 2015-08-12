package cz.encircled.eplayer.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.encircled.eplayer.model.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IOUtil {

    private static final Logger log = LogManager.getLogger();

    private final static Type DEFAULT_TYPE_TOKEN = new TypeToken<Map<String, MediaType>>() {
    }.getType();

    public static <T> T getPlayableJson(@NotNull String filePath) throws IOException {
        return getFromJson(filePath, DEFAULT_TYPE_TOKEN);
    }

    public static <T> T getFromJson(@NotNull String filePath, @NotNull Type token) throws IOException {
        return new Gson().fromJson(new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8"), token);
    }

    public static <T> T getFromJson(@NotNull InputStream is, @NotNull Type token) throws IOException {
        return new Gson().fromJson(new InputStreamReader(is), token);
    }

    public static void storeJson(@NotNull Object obj, @NotNull String path) throws IOException {
        Files.write(Paths.get(path), new Gson().toJson(obj).getBytes("UTF-8"));
    }

    /**
     * @return true if file was created
     */
    public static boolean createIfMissing(@NotNull String pathTo, boolean isDirectory, boolean isJson) {
        Path path = Paths.get(pathTo);
        try {
            if (!Files.exists(path)) {
                if (isDirectory) {
                    Files.createDirectories(path);
                    log.debug("Directory {} not exists and was created", pathTo);
                } else {
                    Path f = Files.createFile(path);
                    if (isJson) {
                        storeJson(new Object(), f.toAbsolutePath().toString());
                    }
                    log.debug("File {} not exists and was created", pathTo);
                }
                return true;
            }
        } catch (IOException io) {
            throw new RuntimeException("Failed to create requeried file " + pathTo, io);
        }
        return false;
    }

    @NotNull
    public static List<File> getFilesInFolder(@NotNull String path) {
        File source = new File(path);
        if (!(source.exists() && source.isDirectory())) {
            throw new IllegalArgumentException();
        }
        return getFilesInFolderInternal(source, new ArrayList<>());
    }

    @NotNull
    private static <T extends Collection<File>> T getFilesInFolderInternal(@NotNull File source, @NotNull T files) {
        File[] filesInFolder = source.listFiles();
        if (filesInFolder != null) {
            for (File file : filesInFolder) {
                if (file.isFile())
                    files.add(file);
                else
                    getFilesInFolderInternal(file, files);
            }
        }
        return files;
    }

}
