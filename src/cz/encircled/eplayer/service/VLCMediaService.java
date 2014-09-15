package cz.encircled.eplayer.service;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.event.*;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.util.LocalizedMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.CountDownLatch;

import static cz.encircled.eplayer.util.LocalizedMessages.ERROR_TITLE;
import static cz.encircled.eplayer.util.LocalizedMessages.MSG_VLC_LIBS_FAIL;

/**
 * Created by Administrator on 9.6.2014.
 */
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

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private EmbeddedMediaPlayer player;

    @Resource
    private ViewService viewService;

    @Nullable
    private String current;

    public static final String VLC_LIB_PATH_64 = "vlc-2.1.5_x64";

    public static final String VLC_LIB_PATH = "vlc-2.1.5";

    @Override
    public boolean isFullScreen(){
        return player.isFullScreen();
    }

    @Override
    public void releasePlayer(){
        if(player != null){
            player.stop();
            player.release();
            current = null;
            currentTime = 0;
        }
    }

    @Override
    public void updateCurrentMediaInCache(){
        if(current != null)
            cacheService.updateEntry(current.hashCode(), currentTime);
    }

    @Override
    public void exitFullScreen(){
        player.setFullScreen(false);
        viewService.exitFullScreen();
    }

    private void play(@NotNull String path, long time){
        CountDownLatch playerCountDown = new CountDownLatch(1);
        viewService.showPlayer(playerCountDown);
        try {
            playerCountDown.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.debug("play: {}", EventQueue.isDispatchThread());
        if(!path.equals(current)){
            log.debug("Path {} is new", path);
            current = path;
            player.prepareMedia(path);
        }
        player.start();
        player.setTime(Math.min(time, player.getLength()));
        eventObserver.fire(Event.SubtitlesUpdated, player.getSpuDescriptions());
        eventObserver.fire(Event.AudioTracksUpdated, player.getAudioDescriptions());
        eventObserver.fire(Event.PlayStart);
    }

    @Override
    public void play(@NotNull String path){
        play(cacheService.createIfAbsent(path));
    }

    @Override
    public void play(@NotNull MediaType p){
        play(p.getPath(), p.getTime());
    }

    @Override
    public void toggleFullScreen() {
        if(player != null && current != null){
            if(player.isFullScreen())
                exitFullScreen();
            else
                enterFullScreen();
        }
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
    public Component getPlayerComponent() {
        return mediaPlayerComponent;
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
        if (player.isPlaying())
             player.pause();
    }

    @Override
    public void togglePlayer() {
        if(player != null && current != null)
            player.pause();
    }

    @Override
    public void stop(){
        if(player != null){
            current = null;
            player.stop();
            viewService.enableSubtitlesMenu(false);
        }
    }

    @PostConstruct
    private void init() {
        long start = System.currentTimeMillis();
        initializeLibs();
        log.trace("VLCMediaService init start");
        try {
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
                @NotNull
                @Override
                protected FullScreenStrategy onGetFullScreenStrategy() {
                    return new Win32FullScreenStrategy(viewService.getWindow());
                }
            };

            player = mediaPlayerComponent.getMediaPlayer();
            player.setEnableMouseInputHandling(false);
            player.setEnableKeyInputHandling(false);

            mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() { // TODO move

                @Override
                public void mouseClicked(@NotNull MouseEvent e) {
                    if(e.getClickCount() == Constants.ONE){
                        togglePlayer();
                    } else {
                        togglePlayer();
                        toggleFullScreen();
                    }
                }
            });

            player.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    if (current != null) {
                        cacheService.updateEntry(current.hashCode(), Constants.ZERO_LONG);
                    }
                    current = null;
                    stop();
                    viewService.showQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    currentTime = newTime;
                    eventObserver.fire(Event.MediaTimeChange, newTime);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    log.error("Failed to open media {} ", current);
                    guiUtil.showMessage(LocalizedMessages.FILE_OPEN_FAILED, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    if(current != null) {
                        cacheService.deleteEntry(current.hashCode());
                        current = null;
                    }
                    viewService.showQuickNavi();
                }

            });

        } catch(Exception e){
            log.error("Player initialization failed", e);
            guiUtil.showMessage("VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
        } catch (NoClassDefFoundError re){
            log.error("Player initialization failed", re);
        }
        log.trace("VLCMediaService init complete in {} ms", System.currentTimeMillis() - start);
    }

    private void initializeLibs(){
        long start = System.currentTimeMillis();
        log.trace("Initialize VLC libs");
        String vlcLibPath = System.getProperty("sun.arch.data.model").equals(X64) ? VLC_LIB_PATH_64 : VLC_LIB_PATH;
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcLibPath);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            log.trace("VLCLib successfully initialized in {} ms", System.currentTimeMillis() - start);
        } catch(UnsatisfiedLinkError e){
            guiUtil.showMessage(localizations.get(MSG_VLC_LIBS_FAIL), localizations.get(ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
            log.error("Failed to load vlc libs from specified path {}", vlcLibPath);
            // TODO exit?
        }
    }

    private void enterFullScreen() {
        player.setFullScreen(true);
        viewService.enterFullScreen();
    }

}
