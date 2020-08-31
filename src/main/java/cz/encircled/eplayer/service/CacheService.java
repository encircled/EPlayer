package cz.encircled.eplayer.service;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.PlayableMedia;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Encircled on 9.6.2014.
 */
public interface CacheService {

    @NotNull
    PlayableMedia createIfAbsent(@NotNull String path);

    @Nullable
    PlayableMedia deleteEntry(@NotNull String id);

    @Nullable
    PlayableMedia updateEntry(@NotNull PlayableMedia media, long time);

    @Nullable
    PlayableMedia getLastByWatchDate();

    void save();

    @NotNull
    List<PlayableMedia> getCache();

    void delayedInit(ApplicationCore core);

}
