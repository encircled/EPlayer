package cz.encircled.eplayer.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import cz.encircled.eplayer.model.Playable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

public class IOUtil {

	private final static Logger log = LogManager.getLogger(PropertyProvider.class);
	
	public final static Type DEFAULT_TYPE_TOKEN = new TypeToken<Map<Integer, Playable>>(){}.getType();
	
	public static <T> T jsonFromFile(@NotNull String filePath, Type classOfT) throws IOException {
		String json = getFileContent(filePath);
		return new Gson().fromJson(json, classOfT);
	}
	
	public static String getFileContent(@NotNull String filePath) throws IOException {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            reader.lines()
                    .parallel()
                    .forEachOrdered((line) -> result.append(line).append("\n"));
		}
		return result.toString();
	}
	
	public static void storeJson(Object obj, String path) throws IOException {
		String json = new Gson().toJson(obj);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))){
			writer.write(json);
			writer.flush();
        }
	}
	
}
