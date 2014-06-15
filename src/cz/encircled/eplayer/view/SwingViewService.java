package cz.encircled.eplayer.view;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
public class SwingViewService implements ViewService {

    private static final Logger log = LogManager.getLogger();

    private CacheService cacheService;

    private MediaService mediaService;

    private FolderScanService folderScanService;

    private Frame frame;

    private int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    @Override
    public void initialize(@NotNull CountDownLatch countDownLatch){
        invokeInEDT(() -> {
            long start = System.currentTimeMillis();
            log.trace("SwingViewService init start");
            frame = new Frame(this, mediaService);
            frame.setVisible(true);
            GUIUtil.setFrame(frame);
            log.trace("SwingViewService init complete in {} ms", System.currentTimeMillis() - start);
            countDownLatch.countDown();
        });
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
    public void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType){
        log.debug("Add tab for folder {}", tabName);
        invokeInEDT(() -> frame.addTabForFolder(tabName, mediaType));
    }

    @Override
    public void updateTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType){
        log.debug("Add tab for folder {}", tabName);
        invokeInEDT(() -> frame.addTabForFolder(tabName, mediaType));
    }

    @Override
    public void showQuickNavi(){
        if(wrapperState != QUICK_NAVI_STATE){
            wrapperState = QUICK_NAVI_STATE;
            invokeInEDT(() -> frame.showQuickNavi(cacheService.getCache()));
        }
    }

    @Override
    public void showPlayer(@NotNull CountDownLatch countDownLatch){
        if(wrapperState != PLAYER_STATE){
            wrapperState = PLAYER_STATE;
            log.debug("Add player to frame");
            invokeInEDT(frame::showPlayer, countDownLatch);
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
    public void setCacheService(@NotNull CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void setFolderScanService(@NotNull FolderScanService folderScanService){
        this.folderScanService = folderScanService;
    }

    @Override
    public void setMediaService(@NotNull MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public void initMediaFiltering(){
        if(isQuickNaviState()){
            log.debug("Show filter input");
            invokeInEDT(frame::showFilterInput);
        }
    }

    @Override
    public void stopMediaFiltering(){
        if(isQuickNaviState()){
            log.debug("Hide filter input");
            invokeInEDT(frame::hideFilterInput);
        }
    }

    @Override
    public void createNewTab(String absolutePath) {
        new SwingWorker<MediaType, Object>(){
            @Override
            protected MediaType doInBackground() throws Exception {
                folderScanService.addIfAbsent(absolutePath);
                folderScanService.scanDirectories();
                return null;
            }
        }.execute();

    }



    @Override
    public void openMedia() {
        invokeInEDT(frame::openMedia);
    }

    private static void invokeInEDT(@NotNull Runnable runnable){
        if(EventQueue.isDispatchThread()){
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private static void invokeInEDT(@NotNull Runnable runnable, @NotNull CountDownLatch countDownLatch){
        if(EventQueue.isDispatchThread()){
            runnable.run();
            countDownLatch.countDown();
        } else {
            Runnable wrapRunnable = () -> {
                    runnable.run();
                    countDownLatch.countDown();
            };
            SwingUtilities.invokeLater(wrapRunnable);
        }
    }

}
