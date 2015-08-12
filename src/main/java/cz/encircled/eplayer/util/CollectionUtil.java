package cz.encircled.eplayer.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Encircled on 16/09/2014.
 */
public class CollectionUtil {

    @NotNull
    public static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

}
