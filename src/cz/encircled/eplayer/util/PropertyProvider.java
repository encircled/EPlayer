package cz.encircled.eplayer.util;

import cz.encircled.eplayer.app.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyProvider {
	
	private final static String PROPERTIES_FILE_PATH = Application.APP_DOCUMENTS_ROOT + "eplayer.properties";
	
	private final static Logger log = LogManager.getLogger(PropertyProvider.class);

	private static Properties properties;

    public final static String SETTING_LANGUAGE = "language";

    public final static String SETTING_DEFAULT_OPEN_LOCATION = "fc_open_location";

    public final static String SETTING_MAX_VOLUME = "max_volume";

    public static void initialize() throws IOException {
		properties = new Properties();
		try (FileInputStream stream = new FileInputStream(getPropertiesFile())){
			properties.load(stream);
		}
	}
	
	public static String get(String key, String defaultValue){
		return properties.getProperty(key, defaultValue);
	}
	
	public static String get(@NotNull String key){
		return properties.getProperty(key);
	}
	
	public static Integer getInt(String key){
		return Integer.parseInt(properties.getProperty(key));
	}
	
	public static Integer getInt(String key, int defaultValue){
		return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
	}
	
	public static void set(@NotNull String key, @Nullable Object value){
        String stringValue = value == null ? null : value.toString();
        log.debug("set {} = {}", key, stringValue);
		properties.setProperty(key, stringValue);
	}
	
	public static void save() throws IOException {
		try(FileOutputStream stream = new FileOutputStream(PROPERTIES_FILE_PATH)){
			properties.store(stream, "user settings");
			log.debug("prop saved");
		} catch(IOException e){
			log.error("Failed to save settings to {}", PROPERTIES_FILE_PATH);
			throw e;
		}
	}
	
	@NotNull
    private static File getPropertiesFile() throws IOException {
        IOUtil.createIfMissing(PROPERTIES_FILE_PATH);
		return new File(PROPERTIES_FILE_PATH);
	}

}
