package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.Playable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface CacheService {

    void forEach(java.util.function.Consumer<Playable> action);

    Playable createIfAbsent(@NotNull String path);

    Playable getEntry(Integer hashCode);

    Playable deleteEntry(int hash);

    CacheService updateEntry(int hash, long time);

    void save();

    Collection<Playable> getCache();

}
