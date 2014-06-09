package cz.encircled.eplayer.view;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.LocalizedMessages;
import cz.encircled.eplayer.view.actions.ActionCommands;
import cz.encircled.eplayer.view.actions.ActionExecutor;
import cz.encircled.eplayer.view.componensts.PlayerControls;
import cz.encircled.eplayer.view.componensts.QuickNaviButton;
import cz.encircled.eplayer.view.listeners.KeyDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.windows.Win32FullScreenStrategy;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static cz.encircled.eplayer.util.GUIUtil.showMessage;

/**
 * Created by Administrator on 9.6.2014.
 */
public class SwingViewService implements ViewService {

    private static final Logger log = LogManager.getLogger();

    private EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private final CacheService cacheService;

    private final ActionExecutor actionExecutor;

    private final MediaService mediaService;

    @Nullable // TODO not null
    private EmbeddedMediaPlayer player;

    @Nullable
    private String current;

    private long currentTime;

    private final Frame frame;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    public boolean isQuickNaviState(){
        return wrapperState == QUICK_NAVI_STATE;
    }

    public boolean isPlayerState(){
        return wrapperState == PLAYER_STATE;
    }

    public SwingViewService(CacheService cacheService, ActionExecutor actionExecutor, MediaService mediaService){
        this.frame = new Frame();
        this.actionExecutor = actionExecutor;
        this.mediaService = mediaService;
        GUIUtil.setFrame(frame);
        this.cacheService = cacheService;
    }

    @Override
    public void play(@NotNull String path, long time){
        if(!showPlayerInternal()){
            showMessage(LocalizedMessages.MSG_VLC_LIBS_FAIL, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!path.equals(current)){
            log.debug("Playing new path {}", path);
            current = path;
            player.prepareMedia(path);
        }
        player.start();
        frame.setSubtitlesToMenu(player.getSpuDescriptions());
        player.setTime(Math.min(time, player.getLength()));
        playerControls.reinitialize();
    }

    public void deletePlayable(int hashCode){
        cacheService.deleteEntry(hashCode);
    }

    public void enterFullScreen(){
        // TODO
        player.setFullScreen(true);
    }

    public void exitFullScreen(){
        // TODO
        player.setFullScreen(false);
    }

    public void togglePlayer() {
        if(player != null && current != null)
            player.pause();
    }

    private boolean showPlayerInternal(){
        if(!app.isVlcAvailable() || player == null)
            return false;
        if(wrapperState != PLAYER_STATE){
            log.debug("Add player to frame");
            wrapperState = PLAYER_STATE;
            tabs.setVisible(false);
            wrapper.add(mediaPlayerComponent, BorderLayout.CENTER); // TODO or not
            wrapper.add(playerControls, BorderLayout.SOUTH);
        }
        return true;
    }

    public void toggleFullScreen() {
        if(player != null && current != null){
            if(player.isFullScreen())
                exitFullScreen();
            else
                enterFullScreen();
        }
    }

    public boolean isFullScreen(){
        return player.isFullScreen();
    }

    public void releasePlayer(){
        if(player != null){
            player.stop();
            player.release();
            current = null;
            currentTime = 0;
        }
    }

    public void updateCurrentMediaInCache(){
        if(current != null)
            cacheService.updateEntry(current.hashCode(), currentTime);
    }

    private void initialize(){
        try {
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
                @Override
                protected FullScreenStrategy onGetFullScreenStrategy() {
                    return new Win32FullScreenStrategy(frame);
                }
            };
            player = mediaPlayerComponent.getMediaPlayer();
            player.setEnableMouseInputHandling(false);
            player.setEnableKeyInputHandling(false);

            mediaPlayerComponent.getVideoSurface().addMouseListener(new MouseAdapter() {

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
                    cacheService.updateEntry(current.hashCode(), Constants.ZERO_LONG);
                    current = null;
                    stopPlayer();
                    showQuickNavi();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    currentTime = newTime;
                    frame.onMediaTimeChange(newTime);
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    showMessage(LocalizedMessages.FILE_OPEN_FAILED, LocalizedMessages.ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                    if(current != null) {
                        cacheService.deleteEntry(current.hashCode());
                        current = null;
                    }
                    showQuickNavi();
                }

            });
            playerControls = new PlayerControls(player);
        } catch(Exception e){
            log.error("Player initialization failed", e);
            showMessage("VLC library not found", "Error title", JOptionPane.ERROR_MESSAGE);
        } catch (NoClassDefFoundError re){
            log.error("Player initialization failed", re);
        }
    }

    public void showQuickNavi(){
        SwingUtilities.invokeLater(() -> {
            if(wrapperState != QUICK_NAVI_STATE){
                updateCurrentMediaInCache();
                if(player != null && player.isFullScreen())
                    exitFullScreen();
                stopPlayer();
                current = null;
                wrapperState = QUICK_NAVI_STATE;

                tabs.setVisible(true);
                wrapper.repaint();
                setTitle(TITLE);
                repaintQuickNavi(); // TODO swing invoke
            }
        });

    }

    void repaintQuickNavi(java.util.List<QuickNaviButton> naviButtons){

        cacheService.forEach((value) -> naviButtons.add(new QuickNaviButton(med, value)));

        SwingUtilities.invokeLater(() -> {
            frame.repaintQuickNavi(cacheService.getCache());
        });
    }

    public void stopPlayer(){
        if(player != null){
            player.stop();
            frame.enableSpuMenu(false);
        }
    }

    private void initializeHotKeys(){
        // TODO frame dependency
        KeyDispatcher dispatcher = new KeyDispatcher(actionExecutor);
        dispatcher.bind(KeyEvent.VK_ENTER, ActionCommands.PLAY_LAST);
        dispatcher.bind(KeyEvent.VK_SPACE, ActionCommands.TOGGLE_PLAYER);
        dispatcher.bind(KeyEvent.VK_ESCAPE, ActionCommands.CANCEL);
        dispatcher.bind(KeyEvent.VK_C, ActionCommands.PLAY_LAST);
        dispatcher.bind(KeyEvent.VK_Q, ActionCommands.EXIT);
        dispatcher.bind(KeyEvent.VK_O, ActionCommands.OPEN);
        dispatcher.bind(KeyEvent.VK_N, ActionCommands.OPEN_QUICK_NAVI);
        dispatcher.bind(KeyEvent.VK_F, ActionCommands.TOGGLE_FULL_SCREEN);
        dispatcher.bind(KeyEvent.VK_S, ActionCommands.SETTINGS);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

}
