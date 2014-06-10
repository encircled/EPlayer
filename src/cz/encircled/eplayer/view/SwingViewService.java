package cz.encircled.eplayer.view;

import com.sun.java.swing.SwingUtilities3;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.view.listeners.KeyDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by Administrator on 9.6.2014.
 */
public class SwingViewService implements ViewService {

    private static final Logger log = LogManager.getLogger();

    private CacheService cacheService;

    private MediaService mediaService;

    private Frame frame;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    private Runnable runWhenReady;

    private boolean isReady = false;

    @Override
    public ViewService initialize(){
        invokeInEDT(() -> {
            long start = System.currentTimeMillis();
            log.trace("SwingViewService init start");
            frame = new Frame(this, mediaService);
            frame.setVisible(true);
            SwingUtilities3.setVsyncRequested(frame, true);// TODO check
            GUIUtil.setFrame(frame);
            initializeHotKeys();
            log.trace("SwingViewService init complete in {} ms", System.currentTimeMillis() - start);
            isReady = true;
            if(runWhenReady != null) {
                new SwingWorker<Object, Object>() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        runWhenReady.run();
                        return null;
                    }
                }.execute();
            }
        });
        return this;
    }

    @Override
    public void deleteMedia(int hashCode){
        new SwingWorker<MediaType, Object>(){
            @Override
            protected MediaType doInBackground() throws Exception {
                MediaType media = cacheService.deleteEntry(hashCode);
                cacheService.save();
                return media;
            }
        }.execute();
    }

    @Override
    public void enterFullScreen(){
        invokeInEDT(frame::enterFullScreen);
    }

    @Override
    public void exitFullScreen(){
        invokeInEDT(frame::exitFullScreen);
    }

    @Override
    public boolean isQuickNaviState(){
        return wrapperState == QUICK_NAVI_STATE;
    }

    @Override
    public boolean isPlayerState(){
        return wrapperState == PLAYER_STATE;
    }

    @Override
    public void showQuickNavi(){
        if(wrapperState != QUICK_NAVI_STATE){
            wrapperState = QUICK_NAVI_STATE;
            invokeInEDT(() -> frame.showQuickNavi(cacheService.getCache()));
        }
    }

    @Override
    public void showPlayer(){
        if(wrapperState != PLAYER_STATE){
            wrapperState = PLAYER_STATE;
            log.debug("Add player to frame");
            invokeInEDT(frame::showPlayer);
        }
    }

    @Override
    public void updateSubtitlesMenu(@NotNull List<TrackDescription> subtitlesDescriptions){
        invokeInEDT(() -> frame.updateSubtitlesMenu(subtitlesDescriptions));
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled){
        invokeInEDT(() -> frame.enableSubtitlesMenu(isEnabled));
    }

    @Override
    public void onMediaTimeChange(long newTime) {
        invokeInEDT(() -> frame.onMediaTimeChange(newTime));
    }

    @Override
    public void onPlayStart(){
        invokeInEDT(frame::reinitializeControls);
    }

    @Override
    public Window getWindow(){
        return frame;
    }

    @Override
    public void onReady(Runnable runnable) {
        if(isReady)
            runnable.run();
        else
            runWhenReady = runnable;
    }

    @Override
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    private static void invokeInEDT(Runnable runnable){
        if(EventQueue.isDispatchThread()){
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private void initializeHotKeys(){
        // TODO frame dependency
        KeyDispatcher dispatcher = new KeyDispatcher(Application.getActionExecutor());
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
