package cz.encircled.eplayer.core;

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
import java.util.concurrent.CountDownLatch;

// TODO Youtube tab

public class Application {

    private static final Logger log = LogManager.getLogger(Application.class);

    public static final String APP_DOCUMENTS_ROOT = System.getenv("APPDATA") + "\\EPlayer\\";

    private FileVisitorManager d;

    private static ActionExecutor actionExecutor;

    private CacheService cacheService;

    private MediaService mediaService;

    private ViewService viewService;

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

        viewService.setMediaService(mediaService);
        viewService.setCacheService(cacheService);

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
            d = new FileVisitorManager(cacheService);
            d.getPaths().forEach((path, result) -> viewService.addTabForFolder(path.toString(), result.values()));
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
//        return d.getPaths().get(Paths.get("C:\\"));
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
