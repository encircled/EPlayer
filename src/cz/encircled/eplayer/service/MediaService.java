package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface MediaService {

    void enterFullScreen();

    void setCacheService(CacheService cacheService);

    void setViewService(ViewService viewService);

    boolean isFullScreen();

    void releasePlayer();

    void updateCurrentMediaInCache();

    void exitFullScreen();

    void play(@NotNull String path);

    void play(@NotNull MediaType p);

    void toggleFullScreen();

    void setSubtitlesById(int id);

    Component getPlayerComponent();

    int getMediaLength();

    int getCurrentTime();

    int getVolume();

    void setVolume(int value);

    void setTime(long value);

    void start();

    void pause();

    void togglePlayer();

    void stop();

    void initialize(CountDownLatch countDownLatch);
}
