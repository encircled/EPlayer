package cz.encircled.eplayer.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.encircled.eplayer.exception.InitializeException;
import cz.encircled.eplayer.util.IOUtil;

public class PropertyProvider {
	
	private final static String PROPERTIES_FILE_NAME = System.getProperty("user.home") + "/eplayer.properties";
	
	private final static Logger log = LogManager.getLogger(PropertyProvider.class);

	private static Properties properties;

    public final static String SETTING_VLC_PATH = "vlc_path";

    public final static String SETTING_QUICK_NAVI_STORAGE_PATH = "quick_navi_path";

    public final static String SETTING_LANGUAGE = "language";

    public final static String SETTING_DEFAULT_OPEN_LOCATION = "fc_open_location";

    public final static String SETTING_MAX_VOLUME = "max_volume";

    public static void initialize(){
		properties = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(getPropertiesFile());
			properties.load(stream);
		} catch(Exception e){
			log.error("failed to read props from {}", PROPERTIES_FILE_NAME);
			throw new InitializeException(e.getMessage());
		} finally {
			IOUtil.close(stream);
			stream = null;
		}
	}
	
	public static String get(String key, String defaultValue){
		return properties.getProperty(key, defaultValue);
	}
	
	public static String get(String key){
		return properties.getProperty(key);
	}
	
	public static Integer getInt(String key){
		return Integer.parseInt(properties.getProperty(key));
	}
	
	public static Integer getInt(String key, int defaultValue){
		return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
	}
	
	public static void set(String key, Object value){
		properties.setProperty(key, value == null ? null : value.toString());
	}
	
	public static void save() throws IOException {
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(PROPERTIES_FILE_NAME);
			properties.store(stream, "user settings");
			log.debug("prop saved");
		} catch(IOException e){
			log.error("Failed to save settings to {}", PROPERTIES_FILE_NAME);
			throw e;
		} finally {
			IOUtil.close(stream);
			stream = null;
		}
	}
	
	public Set<Entry<Object, Object>> getEntrySet(){
		return properties.entrySet();
	}
	
	private final static File getPropertiesFile() throws IOException {
		File f = new File(PROPERTIES_FILE_NAME);
		if(!f.exists()){
			log.debug("prop file doesn't exists, creating new");
			if(!f.createNewFile())
				throw new IOException("Failed to create property file");
		}
		return f;
	}

}
