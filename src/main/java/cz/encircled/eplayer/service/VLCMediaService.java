package cz.encircled.eplayer.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.elight.core.annotation.Component;
import cz.encircled.elight.core.annotation.Wired;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.view.fx.PlayerScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
@Component
public class VLCMediaService implements MediaService {

    // TODO smthing
    public static final String VLC_LIB_PATH = "E:\\soft\\vlc-2.2.1";
    private static final Logger log = LogManager.getLogger();
    @Wired
    private CacheService cacheService;
    @Wired
    private GuiUtil guiUtil;
    @Wired
    private EventObserver eventObserver;
    private long currentTime;
    private MediaPlayer player;
    @Wired
    private ViewService viewService;
    @Nullable
    private String current;
    @Wired
    private ActionExecutor actionExecutor;
    @Wired
    private PlayerScreen playerScreen;

    public VLCMediaService() {
        initializeLibs();
    }

    public static void main(String[] args) {
        JFrame test = new JFrame("Test");
        test.setVisible(true);
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_LIB_PATH);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
        } catch (UnsatisfiedLinkError e) {
        }
        new Thread(() -> {
            HeadlessMediaPlayer headlessMediaPlayer = new MediaPlayerFactory().newHeadlessMediaPlayer();
            headlessMediaPlayer.prepareMedia("E:\\video\\ostrov.mkv");
            headlessMediaPlayer.start();
        }).start();


    }

    @Override
    public void releasePlayer() {
        stop();
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void updateCurrentMediaInCache() {
        if (current != null)
            cacheService.updateEntry(current, currentTime);
    }

    private void play(@NotNull String path, long time) {
        log.debug("Play {}, start time is {}", path, time);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        log.debug("Show player requested");
        viewService.showPlayer(countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("Show player done");
        if (!path.equals(current)) {
            log.debug("Path {} is new", path);
            current = path;
            player.prepareMedia(path);
        }
        player.start();
        setTime(Math.min(time, player.getLength()));
        log.debug("Playing started");
    }

    @Override
    public void play(@NotNull String path) {
        play(cacheService.createIfAbsent(path));
    }

    @Override
    public void play(@NotNull MediaType p) {
        cacheService.addIfAbsent(p);
        play(p.getPath(), p.getTime());
    }

    @Override
    public int getSubtitles() {
        return player.getSpu();
    }

    @Override
    public void setSubtitles(int id) {
        player.setSpu(id);
    }

    @Override
    public int getAudioTrack() {
        return player.getAudioTrack();
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
    public long getCurrentTime() {
        return currentTime;
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
    public void toggle() {
        player.pause();
    }

    @Override
    public void pause() {
        if (player.isPlaying())
            player.pause();
    }

    @Override
    public void stop() {
        log.debug("Stop player");
        current = null;
        currentTime = 0L;
        if(player != null) {
            player.stop();
        }
        viewService.enableSubtitlesMenu(false);
    }

    @PostConstruct
    private void init() {
        long start = System.currentTimeMillis();
        log.trace("VLCMediaService init start");
        try {
            player = playerScreen.getMediaPlayerComponent().getMediaPlayer();
//            player = new MediaPlayerFactory().newHeadlessMediaPlayer();

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
                public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
                    log.debug("Media parsed - {}", current);
                    eventObserver.fire(Event.subtitlesUpdated, player.getSpuDescriptions());
                    eventObserver.fire(Event.audioTracksUpdated, player.getAudioDescriptions());
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    currentTime = 0L;
                    actionExecutor.execute(ActionCommands.OPEN_QUICK_NAVI);
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
                    guiUtil.showMessage(Localization.fileOpenFailed.ln(), Localization.errorTitle.ln());
                    if (current != null) {
                        cacheService.deleteEntry(current);
                        current = null;
                    }
                    viewService.switchToQuickNavi();
                }

            });

        } catch (Exception e) {
            log.error("Player initialization failed", e);
            guiUtil.showMessage("VLC library not found", "Error title");
        }
        log.trace("VLCMediaService init complete in {} ms", System.currentTimeMillis() - start);
    }

    private void initializeLibs() {
        log.trace("Initialize VLC libs");
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_LIB_PATH);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            log.trace("VLCLib successfully initialized");
        } catch (UnsatisfiedLinkError e) {
            // TODO NPE
//            guiUtil.showMessage(Localizations.get(MSG_VLC_LIBS_FAIL), Localizations.get(errorTitle), JOptionPane.ERROR_MESSAGE);
            log.error("Failed to load vlc libs from specified path {}", VLC_LIB_PATH);
            // TODO exit?
        }
    }

}
