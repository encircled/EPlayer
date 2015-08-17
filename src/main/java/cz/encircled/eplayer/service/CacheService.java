package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Encircled on 9.6.2014.
 */
public interface CacheService {

    void forEach(@NotNull Consumer<MediaType> action);

    @NotNull
    MediaType createIfAbsent(@NotNull String path);

    @NotNull
    MediaType addIfAbsent(@NotNull MediaType mediaType);

    @Nullable
    MediaType getEntry(@NotNull String id);

    @Nullable
    MediaType deleteEntry(@NotNull String id);

    @Nullable
    MediaType updateEntry(@NotNull String id, long time);

    @Nullable
    MediaType getLastByWatchDate();

    void save();

    @NotNull
    List<MediaType> getCache();

    void init();

}
