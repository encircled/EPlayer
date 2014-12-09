package cz.encircled.eplayer.util;

import com.google.gson.reflect.TypeToken;
import cz.encircled.eplayer.core.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingsProvider {

    private final static String PROPERTIES_FILE_PATH = Application.APP_DOCUMENTS_ROOT + "eplayer.properties.json";

    private final static Logger log = LogManager.getLogger(SettingsProvider.class);

    private static Map<String, Object> properties;

    private final static Type TYPE_TOKEN = new TypeToken<Map<String, Object>>() {
    }.getType();

    static {
        try {
            properties = IOUtil.createIfMissing(PROPERTIES_FILE_PATH, false, true) ? new HashMap<>() : IOUtil.getFromJson(PROPERTIES_FILE_PATH, TYPE_TOKEN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(@NotNull String key, String defaultValue) {
        return properties.containsKey(key) ? get(key, String.class) : defaultValue;
    }

    @NotNull
    public static List<String> getList(@NotNull String key) {
        List<?> list = get(key, List.class);
        if (list == null)
            return new ArrayList<>(0);
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    public static String get(@NotNull String key) {
        return get(key, String.class);
    }

    public static Integer getInt(String key) {
        Number n = get(key, Number.class);
        return n == null ? null : n.intValue();
    }

    public static Integer getInt(String key, int defaultValue) {
        Number n = get(key, Number.class);
        return n == null ? defaultValue : n.intValue();
    }

    public static void set(@NotNull String key, @Nullable Object value) {
        log.debug("Set property {} = {}", key, value);
        properties.put(key, value);
    }

    public static void addToList(@NotNull String key, @Nullable String value) {
        List<String> list = getList(key);
        list.add(value);
        set(key, list);
    }

    public static void removeFromList(@NotNull String key, @Nullable String value) {
        List<String> list = getList(key);
        if (list.contains(value)) {
            list.remove(value);
            set(key, list);
        }
    }

    public static void save() {
        try {
            IOUtil.storeJson(properties, PROPERTIES_FILE_PATH);
            log.debug("Properties saved");
        } catch (IOException io) {
            log.error("Failed to save properties to {}", PROPERTIES_FILE_PATH, io);
        }
    }

    private static <T> T get(String key, Class<T> clazz) {
        return (T) properties.get(key);
    }

}
