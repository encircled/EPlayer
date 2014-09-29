package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface MediaService {

    void releasePlayer();

    void updateCurrentMediaInCache();

    void play(@NotNull String path);

    /**
     * @param p
     */
    void play(@NotNull MediaType p);

    int getMediaLength();

    long getCurrentTime();

    int getVolume();

    void setVolume(int value);

    void setTime(long value);

    void start();

    void toggle();

    void pause();

    void stop();

    void setSubtitles(int id);

    int getSubtitles();

    int getAudioTrack();

    void setAudioTrack(int trackID);

    boolean isPlaying();

}
