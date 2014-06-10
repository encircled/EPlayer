package cz.encircled.eplayer.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import cz.encircled.eplayer.model.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class IOUtil {

    private static final Logger log = LogManager.getLogger();

	private final static Type DEFAULT_TYPE_TOKEN = new TypeToken<Map<Integer, MediaType>>(){}.getType();
	
	public static <T> T getPlayableJson(@NotNull String filePath) throws IOException {
		return new Gson().fromJson(new String(Files.readAllBytes(Paths.get(filePath))), DEFAULT_TYPE_TOKEN);
	}
	
	public static void storeJson(@NotNull Object obj, @NotNull String path) throws IOException {
        Files.write(Paths.get(path), new Gson().toJson(obj).getBytes("UTF-8"));
	}

    /**
     * @return true if file was created
     */
    public static boolean createIfMissing(@NotNull String pathToFile) throws IOException {
        return createIfMissing(pathToFile, false);
    }

    public static boolean createIfMissing(@NotNull String pathTo, boolean isDirectory) throws IOException {
        Path path = Paths.get(pathTo);
        if(!Files.exists(path)) {
            if(isDirectory) {
                Files.createDirectories(path);
                log.debug("Directory {} not exists and was created", pathTo);
            } else {
                Files.createFile(path);
                log.debug("File {} not exists and was created", pathTo);
            }
            return true;
        }
        return false;
    }

}
