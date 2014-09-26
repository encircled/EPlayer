package cz.encircled.eplayer.view.swing;

/**
 * Created by Encircled on 14/09/2014.
 */
public class SwingFromGuiViewService {
} /* implements FromGuiViewService {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private CacheService cacheService;

    @Resource
    private FolderScanService folderScanService;

    @Resource
    private Settings settings;

    @Resource
    private MediaService mediaService;

    @Override
    public void play(MediaType media) {
        new Thread(() -> mediaService.play(media.getPath())).start();
    }

    @Override
    public void createNewTab(String absolutePath) {
        new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                if (folderScanService.addIfAbsent(absolutePath)) {
                    settings.addToList(Settings.FOLDERS_TO_SCAN, absolutePath);
                    settings.save();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void removeTabForFolder(@NotNull String tabName) {
        log.debug("Remove tab for folder {}", tabName);
        new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                folderScanService.removeFolder(tabName);
                settings.removeFromList(Settings.FOLDERS_TO_SCAN, tabName);
                settings.save();
                return null;
            }
        }.execute();
    }

    @Override
    public void deleteMedia(int hashCode) {
        new Thread(() -> {
            cacheService.deleteEntry(hashCode);
            cacheService.save();
        }).start();
    }

}     */
