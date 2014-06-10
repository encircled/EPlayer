package cz.encircled.eplayer.service;

import uk.co.caprica.vlcj.player.TrackDescription;

import java.awt.*;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface ViewService {

    boolean isQuickNaviState();

    boolean isPlayerState();

    void deleteMedia(int hashCode);

    void enterFullScreen();

    void exitFullScreen();

    void showPlayer();

    void onPlayStart();

    void updateSubtitlesMenu(java.util.List<TrackDescription> spuDescriptions);

    void enableSubtitlesMenu(boolean isEnabled);

    void showQuickNavi();

    void onMediaTimeChange(long newTime);

    void setCacheService(CacheService cacheService);

    void setMediaService(MediaService mediaService);

    ViewService initialize();

    Window getWindow();

    void onReady(Runnable runnable);

}
