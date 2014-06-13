package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import java.awt.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface ViewService {

    boolean isQuickNaviState();

    boolean isPlayerState();

    void deleteMedia(int hashCode);

    void enterFullScreen();

    void exitFullScreen();

    void showPlayer(@NotNull CountDownLatch countDownLatch);

    void onPlayStart();

    void updateSubtitlesMenu(java.util.List<TrackDescription> spuDescriptions);

    void enableSubtitlesMenu(boolean isEnabled);

    void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType);

    void updateTabForFolder(@NotNull String path, @NotNull Collection<MediaType> values);

    void nextFolderTab();

    void showQuickNavi();

    void onMediaTimeChange(long newTime);

    void setCacheService(@NotNull CacheService cacheService);

    void setMediaService(@NotNull MediaService mediaService);

    void initialize(@NotNull CountDownLatch countDownLatch);

    Window getWindow();

    void initMediaFiltering();

    void stopMediaFiltering();

}
