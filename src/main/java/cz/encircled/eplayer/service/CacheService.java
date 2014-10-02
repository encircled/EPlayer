package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface CacheService {

    void forEach(Consumer<MediaType> action);

    MediaType createIfAbsent(@NotNull String path);

    MediaType addIfAbsent(@NotNull MediaType mediaType);

    MediaType getEntry(Integer hashCode);

    MediaType deleteEntry(int hash);

    MediaType updateEntry(int hash, long time);

    @Nullable
    MediaType getLastByWatchDate();

    void save();

    List<MediaType> getCache();

}
