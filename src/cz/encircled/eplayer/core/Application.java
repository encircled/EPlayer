package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.*;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.service.action.ReflectionActionExecutor;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.MessagesProvider;
import cz.encircled.eplayer.util.PropertyProvider;
import cz.encircled.eplayer.view.SwingViewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static cz.encircled.eplayer.util.PropertyProvider.*;
import static cz.encircled.eplayer.util.PropertyProvider.FOLDERS_TO_SCAN;

// TODO Youtube tab and filtering

public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    private FileVisitorScanService folderScanService;

    private static ActionExecutor actionExecutor;

    private CacheService cacheService;

    private MediaService mediaService;

    private ViewService viewService;

    private FileScanListener fileScanListener = new FileScanListener() {

        @Override
        public void onFolderScanned(String path, Map<Integer, MediaType> media) {
            viewService.addTabForFolder(path, media.values());
        }

        @Override
        public void onFolderChange(String path, Map<Integer, MediaType> media) {
            viewService.updateTabForFolder(path, media.values());
        }
    };

    public static ActionExecutor getActionExecutor(){
        return actionExecutor;
    }

    public void reinitialize() throws IOException {
        log.trace("App init");
        // TODO what do we need here?
//        if(frame != null){
//            frame.stop();
//            frame.dispose();
//            frame = null;
//        }
        log.trace("Reinitializing completed");
    }

    private void initialize(String[] arguments) throws IOException, InterruptedException {
        log.trace("App init");
        IOUtil.createIfMissing(APP_DOCUMENTS_ROOT, true);
        PropertyProvider.initialize();
        log.trace("Properties init success");
        MessagesProvider.initialize();
        log.trace("Messages init success");

        actionExecutor = new ReflectionActionExecutor();
        cacheService = new JsonCacheService();
        viewService = new SwingViewService();
        mediaService = new VLCMediaService();
        folderScanService = new FileVisitorScanService(cacheService);

        viewService.setMediaService(mediaService);
        viewService.setCacheService(cacheService);
        viewService.setFolderScanService(folderScanService);

        mediaService.setCacheService(cacheService);
        mediaService.setViewService(viewService);

        actionExecutor.setCacheService(cacheService);
        actionExecutor.setMediaService(mediaService);
        actionExecutor.setViewService(viewService);

        CountDownLatch viewCountDown = new CountDownLatch(1);

        viewService.initialize(viewCountDown);
        viewCountDown.await();
        
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        new Thread(() -> cacheService.initialize(countDownLatch)).start();
        mediaService.initialize(countDownLatch);

        countDownLatch.await();
        if(arguments.length == 0)
            viewService.showQuickNavi();
        else
            mediaService.play(arguments[0]);

        new Thread(() -> {
            folderScanService.initialize()
                                .addAllIfAbsent(getArray(FOLDERS_TO_SCAN, FOLDER_SEPARATOR))
                                .addFiledScanListener(fileScanListener)
                                .start();
//            folderScanService.getPaths().forEach((path, result) -> viewService.addTabForFolder(path.toString(), result.values()));
        }).start();

        addCloseHook();

        log.trace("Init complete");
    }

//    private void initializeGui(String openWhenReady){
//        SwingUtilities.invokeLater(() -> {
//
//            TODO
//
//            if(StringUtil.isSet(openWhenReady)){
//                frame.addWindowListener(new WindowAdapter() {
//                    @Override
//                    public void windowActivated(WindowEvent e) {
//                        mediaService.play(openWhenReady);
//                        frame.removeWindowListener(this);
//                    }
//                });
//            } else {
//                viewService.showQuickNavi();
//            }
//
//            frame.run();
//        });
//    }


//    public Map<Integer, MediaType> getTest(){
//        return folderScanService.getPaths().get(Paths.get("C:\\"));
//    }

    private void addCloseHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                mediaService.updateCurrentMediaInCache();
                mediaService.releasePlayer();
            }
        });
        log.trace("Close hook added");
    }

    public static void main(final String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            new Application().initialize(args);
        } catch (Throwable e) {
            log.error(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
