package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.Playable;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface MediaService {

    void play(@NotNull String path);

    void play(@NotNull Playable p);
}
