package cz.encircled.eplayer.view;

import cz.encircled.eplayer.service.ActionExecutor;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.view.actions.ActionCommands;
import cz.encircled.eplayer.view.listeners.KeyDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Created by Administrator on 9.6.2014.
 */
public class SwingViewService implements ViewService {

    private static final Logger log = LogManager.getLogger();

    private CacheService cacheService;

    private ActionExecutor actionExecutor;

    private MediaService mediaService;

    private Frame frame;

    private volatile int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    @Override
    public void initialize(){
        frame = new Frame(this, mediaService);
        GUIUtil.setFrame(frame);
        initializeHotKeys();
        frame.setVisible(true);
    }

    @Override
    public void deleteMedia(int hashCode){
        cacheService.deleteEntry(hashCode);
    }

    @Override
    public void enterFullScreen(){
        SwingUtilities.invokeLater(frame::enterFullScreen);
    }

    @Override
    public void exitFullScreen(){
        SwingUtilities.invokeLater(frame::exitFullScreen);
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
            SwingUtilities.invokeLater(() -> frame.showQuickNavi(cacheService.getCache()));
        }
    }

    @Override
    public void showPlayer(){
        if(wrapperState != PLAYER_STATE){
            wrapperState = PLAYER_STATE;
            log.debug("Add player to frame");
            frame.showPlayer();
        }
    }

    @Override
    public void updateSubtitlesMenu(List<TrackDescription> subtitlesDescriptions){
        SwingUtilities.invokeLater(() -> frame.updateSubtitlesMenu(subtitlesDescriptions));
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled){
        SwingUtilities.invokeLater(() -> frame.enableSubtitlesMenu(isEnabled));
    }

    @Override
    public void onMediaTimeChange(long newTime) {
        SwingUtilities.invokeLater(() -> frame.onMediaTimeChange(newTime));
    }

    @Override
    public void onPlayStart(){
        SwingUtilities.invokeLater(() -> frame.reinitializeControls());
    }

    @Override
    public Window getWindow(){
        return frame;
    }

    @Override
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void setActionExecutor(ActionExecutor actionExecutor) {
        this.actionExecutor = actionExecutor;
    }

    @Override
    public void setMediaService(MediaService mediaService) {
        this.mediaService = mediaService;
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
