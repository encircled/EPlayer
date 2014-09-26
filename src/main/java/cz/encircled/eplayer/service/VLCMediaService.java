package cz.encircled.eplayer.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.view.AppView;
import cz.encircled.eplayer.view.fx.PlayerScene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.*;

import java.util.concurrent.CountDownLatch;

import static cz.encircled.eplayer.util.LocalizedMessages.ERROR_TITLE;
import static cz.encircled.eplayer.util.LocalizedMessages.MSG_VLC_LIBS_FAIL;

/**
 * Created by Administrator on 9.6.2014.
 */
@Resource
public class VLCMediaService implements MediaService {

    private static final Logger log = LogManager.getLogger();
    public static final String X64 = "64";

    @Resource
    private CacheService cacheService;

    @Resource
    private GuiUtil guiUtil;

    @Resource
    private Localizations localizations;

    @Resource
    private EventObserver eventObserver;

    private long currentTime;

    private DirectMediaPlayer player;

    //    @Resource
    private AppView appView;

    @Resource
    private ViewService viewService;

    @Nullable
    private String current;

    public static final String VLC_LIB_PATH_64 = "vlc/vlc-2.1.5_x64";

    public static final String VLC_LIB_PATH = "vlc/vlc-2.1.5";

    public VLCMediaService() {
        initializeLibs();
    }

    @Override
    public void releasePlayer() {
        player.stop();
        player.release();
        current = null;
        currentTime = 0L;
    }

    @Override
    public void updateCurrentMediaInCache() {
        if (current != null)
            cacheService.updateEntry(current.hashCode(), currentTime);
    }

    private void play(@NotNull String path, long time) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        viewService.showPlayer(countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!path.equals(current)) {
            log.debug("Path {} is new", path);
            current = path;
            player.prepareMedia(path);
        }
        player.start();
        player.setTime(Math.min(time, player.getLength()));
        eventObserver.fire(Event.subtitlesUpdated, player.getSpuDescriptions());
        eventObserver.fire(Event.audioTracksUpdated, player.getAudioDescriptions());
    }

    @Override
    public void play(@NotNull String path) {
        play(cacheService.createIfAbsent(path));
    }

    @Override
    public void play(@NotNull MediaType p) {
        play(p.getPath(), p.getTime());
    }

    @Override
    public void setSubtitles(int id) {
        player.setSpu(id);
    }

    @Override
    public void setAudioTrack(int trackId) {
        player.setAudioTrack(trackId);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getMediaLength() {
        return (int) player.getLength();
    }

    @Override
    public int getCurrentTime() {
        return (int) player.getTime();
    }

    @Override
    public int getVolume() {
        return player.getVolume();
    }

    @Override
    public void setVolume(int value) {
        player.setVolume(value);
    }

    @Override
    public void setTime(long value) {
        player.setTime(value);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public void pause() {
        if (player.canPause())
            player.pause();
    }

    @Override
    public void stop() {
        current = null;
        player.stop();
        viewService.enableSubtitlesMenu(false);
    }

    @Resource
    private PlayerScene playerScene;

    @PostConstruct
    private void init() {
        long start = System.currentTimeMillis();

        log.trace("VLCMediaService init start");
        try {
            player = playerScene.getMediaPlayerComponent().getMediaPlayer();
                                    /*
            mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() { // TODO move

                @Override
                public void mouseClicked(@NotNull MouseEvent e) {
                    if(e.getClickCount() == Constants.ONE){
                        togglePlayer();
                    } else {
                        togglePlayer();
                    }
                }
            });            */

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    eventObserver.fire(Event.playingChanged, true);
                }

                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    eventObserver.fire(Event.playingChanged, false);
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    if (current != null) {
                        cacheService.updateEntry(current.hashCode(), Constants.ZERO_LONG);
                    }
                    current = null;
                    stop();
                    viewService.switchToQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    currentTime = newTime;
                    eventObserver.fire(Event.mediaTimeChange, newTime, null);
                }

                @Override
                public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
                    eventObserver.fire(Event.mediaDurationChange, newDuration, null);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    log.error("Failed to open media {} ", current);
                    guiUtil.showMessage(LocalizedMessages.FILE_OPEN_FAILED, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    if (current != null) {
                        cacheService.deleteEntry(current.hashCode());
                        current = null;
                    }
                    viewService.switchToQuickNavi();
                }

            });
        } catch (Exception e) {
            log.error("Player initialization failed", e);
            guiUtil.showMessage("VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
        }
        log.trace("VLCMediaService init complete in {} ms", System.currentTimeMillis() - start);
    }

    private void initializeLibs() {
        long start = System.currentTimeMillis();
        log.trace("Initialize VLC libs");
        String vlcLibPath = System.getProperty("sun.arch.data.model").equals(X64) ? VLC_LIB_PATH_64 : VLC_LIB_PATH;
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibPath);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            log.trace("VLCLib successfully initialized in {} ms", System.currentTimeMillis() - start);
        } catch (UnsatisfiedLinkError e) {
            // TODO NPE
            guiUtil.showMessage(localizations.get(MSG_VLC_LIBS_FAIL), localizations.get(ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            log.error("Failed to load vlc libs from specified path {}", vlcLibPath);
            // TODO exit?
        }
    }

}
