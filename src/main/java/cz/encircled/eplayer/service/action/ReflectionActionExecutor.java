package cz.encircled.eplayer.service.action;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.gui.ViewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.TreeMap;

@Resource
public class ReflectionActionExecutor implements ActionExecutor {

    private final static Logger log = LogManager.getLogger(ReflectionActionExecutor.class);

    private TreeMap<String, Method> commands;

    @Resource
    private MediaService mediaService;

    @Resource
    private CacheService cacheService;

    @Resource
    private ViewService viewService;

    public ReflectionActionExecutor() {
        log.debug("Action executor init");
        commands = new TreeMap<>();
        Field[] commandFields = ActionCommands.class.getDeclaredFields();
        Arrays.stream(ActionCommands.class.getDeclaredFields()).forEach(field -> {
            try {
                commands.put((String) field.get(null), ReflectionActionExecutor.class.getMethod((String) field.get(null)));
            } catch (Exception e) {
                log.warn("error reading command field {} ", field.getName());
            }
        });
    }

    // TODO: create Object for commands with type. When command is GUI only - do not manipulate with threads
    @Override
    public void execute(String command) {
        try {
            if (EventQueue.isDispatchThread()) {
                log.debug("Execute is called from EDT, creating SwingWorker");
                new SwingWorker<Object, Object>() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        return commands.get(command).invoke(ReflectionActionExecutor.this);
                    }
                }.execute();
            } else {
                commands.get(command).invoke(ReflectionActionExecutor.this);
            }

        } catch (Throwable e) {
            log.error("Failed to execute command {}, msg:", command, e.getMessage());
        }
    }

    public void exit() {
        System.exit(Constants.ZERO);
    }

    public void openMedia() {
        viewService.openMedia();
    }

    public void showShutdownTimeChooser() {
//        frame.showShutdownTimeChooser(); TODO
    }

    public void openQuickNavi() {
        mediaService.pause();
        viewService.switchToQuickNavi();
        mediaService.updateCurrentMediaInCache();
        mediaService.stop();
        cacheService.save();
    }

    public void togglePlayer() {
        mediaService.pause();
    }

    public void toggleFullScreen() {
//        mediaService.toggleFullScreen();
    }

    public void back() {
        log.debug("Execute back");
//        if (viewService.isPlayerState()) {
//            if (mediaService.isFullScreen()) {
//                mediaService.exitFullScreen();
//                mediaService.updateCurrentMediaInCache();
//            } else
//                execute(ActionCommands.OPEN_QUICK_NAVI);
//        }
    }

    public void playLast() {
        MediaType media = cacheService.getLastByWatchDate();
        if (media != null)
            mediaService.play(media);
    }

    public void mediaFiltering() {
        viewService.initMediaFiltering();
    }

    public void stopMediaFiltering() {
        viewService.stopMediaFiltering();
    }

}
