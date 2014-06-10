package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import java.awt.*;
import java.util.Collection;
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

    void showPlayer(CountDownLatch countDownLatch);

    void onPlayStart();

    void updateSubtitlesMenu(java.util.List<TrackDescription> spuDescriptions);

    void enableSubtitlesMenu(boolean isEnabled);

    void addTabForFolder(String tabName, @NotNull Collection<MediaType> mediaType);

    void showQuickNavi();

    void onMediaTimeChange(long newTime);

    void setCacheService(CacheService cacheService);

    void setMediaService(MediaService mediaService);

    void initialize(CountDownLatch countDownLatch);

    Window getWindow();

}
