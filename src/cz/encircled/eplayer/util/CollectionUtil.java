package cz.encircled.eplayer.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Encircled on 16/09/2014.
 */
public class CollectionUtil {

    public static <K, V> Map<K, V> createMap(K key, V value) {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

}
