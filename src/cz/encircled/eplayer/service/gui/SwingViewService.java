package cz.encircled.eplayer.service.gui;

/**
 * Created by Administrator on 9.6.2014.
 */

public class SwingViewService {
} /* implements ViewService {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private CacheService cacheService;

    @Resource
    private FolderScanService folderScanService;

    @Resource
    private cz.encircled.eplayer.view.swing.Frame frame;

    @Resource
    private Settings settings;

    @Resource
    private EventObserver eventObserver;

    private int wrapperState = -1;

    private final static int QUICK_NAVI_STATE = 0;

    private final static int PLAYER_STATE = 1;

    @PostConstruct
    private void init() {
        eventObserver.listen(Event.MediaTimeChange, (event, args) -> invokeInEDT(() -> frame.onMediaTimeChange((Long) args[0])));
        frame.setVisible(true);
        GuiUtil.setFrame(frame);
        eventObserver.listen(Event.FolderChanged, (event, args) -> {
            invokeInEDT(() -> frame.addTabForFolder((String) args[0], ((Map<Integer, MediaType>) args[1]).values()));
        });
        eventObserver.listen(Event.FolderScanned, (event, args) -> {
            invokeInEDT(() -> frame.addTabForFolder((String) args[0], ((Map<Integer, MediaType>) args[1]).values()));
        });
        eventObserver.listen(Event.SubtitlesUpdated, (event, args) -> {
            invokeInEDT(() -> frame.updateSubtitlesMenu((List<TrackDescription>) args[0]));
        });
        eventObserver.listen(Event.PlayStart, (event, args) -> invokeInEDT(frame::reinitializeControls));
        eventObserver.listen(Event.AudioTracksUpdated, (event, args) -> {
            invokeInEDT(() -> frame.updateAudioTracksMenu((List<TrackDescription>) args[0]));
        });

        folderScanService.addAllIfAbsent(settings.getList(Settings.FOLDERS_TO_SCAN)).start();
    }

    @Override
    public void enterFullScreen() {
        invokeInEDT(frame::enterFullScreen);
    }

    @Override
    public void exitFullScreen() {
        invokeInEDT(frame::exitFullScreen);
    }

    @Override
    public boolean isPlayerState() {
        return wrapperState == PLAYER_STATE;
    }

    @Override
    public void showQuickNavi() {
        if (wrapperState != QUICK_NAVI_STATE) {
            wrapperState = QUICK_NAVI_STATE;
            Collection<MediaType> media = cacheService.getCache();
            invokeInEDT(() -> frame.showQuickNavi(media));
        }
    }

    @Override
    public void showPlayer(@NotNull CountDownLatch countDownLatch) {
        if (wrapperState != PLAYER_STATE) {
            wrapperState = PLAYER_STATE;
            log.debug("Add player to frame");
            invokeInEDT(frame::showPlayer, countDownLatch);
        }
    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {
        invokeInEDT(() -> frame.enableSubtitlesMenu(isEnabled));
    }

    @Override
    public Window getWindow() {
        return frame;
    }

    @Override
    public void initMediaFiltering() {
        if (isQuickNaviState()) {
            log.debug("Show filter input");
            invokeInEDT(frame::showFilterInput);
        }
    }

    @Override
    public void stopMediaFiltering() {
        if (isQuickNaviState()) {
            log.debug("Hide filter input");
            invokeInEDT(frame::hideFilterInput);
        }
    }

    @Override
    public void openMedia() {
        invokeInEDT(frame::openMedia);
    }

    private static void invokeInEDT(@NotNull Runnable runnable) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private static void invokeInEDT(@NotNull Runnable runnable, @NotNull CountDownLatch countDownLatch) {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
            countDownLatch.countDown();
        } else {
            SwingUtilities.invokeLater(() -> {
                runnable.run();
                countDownLatch.countDown();
            });
        }
    }

    private boolean isQuickNaviState() {
        return wrapperState == QUICK_NAVI_STATE;
    }

}
                                   */