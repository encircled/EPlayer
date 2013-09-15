package cz.encircled.eplayer.util;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cz.encircled.eplayer.app.PropertyProvider;
import cz.encircled.eplayer.model.Playable;

public class IOUtil {

	private final static Logger log = LogManager.getLogger(PropertyProvider.class);
	
	public final static Type DEFAULT_TYPE_TOKEN = new TypeToken<Map<Integer, Playable>>(){}.getType();
	
	public static void close(Closeable c){
		if(c != null){
			try {
				c.close();
 			} catch(Exception e){
 				try {
 					c.close();
 				} catch(Exception critical){
 					log.error("Failed to close {}", c.toString());
 				}
 			}
			c = null;
		}
	}
	
	public static <T> T jsonFromFile(String filepath, Type classOfT) throws IOException {
		String json = getFileContent(filepath);
		return new Gson().fromJson(json, classOfT);
	}
	
	public static String getFileContent(String filepath) throws IOException {
		BufferedReader reader = null;
		StringBuilder result = new StringBuilder();
		String buffer;
		try {
			reader = new BufferedReader(new FileReader(filepath));
			while((buffer = reader.readLine()) != null)
				result.append(buffer).append("\n");
		} finally {
			close(reader);
		}
		return result.toString();
	}
	
	public static void storeJson(Object obj, String path) throws IOException {
		String json = new Gson().toJson(obj);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(path));
			writer.write(json);
			writer.flush();
        } finally {
			close(writer);
			writer = null;
		}
	}
	
}
