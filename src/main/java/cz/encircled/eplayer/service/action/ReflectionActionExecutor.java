package cz.encircled.eplayer.service.action;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.gui.ViewService;
import cz.encircled.eplayer.util.ReflectionUtil;
import cz.encircled.eplayer.view.AppView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.TreeMap;

@Resource
public class ReflectionActionExecutor implements ActionExecutor {

    private final static Logger log = LogManager.getLogger();

    private TreeMap<String, Method> commands;

    @Resource
    private MediaService mediaService;

    @Resource
    private CacheService cacheService;

    @Resource
    private ViewService viewService;

    @Resource
    private AppView appView;

    public ReflectionActionExecutor() {
        log.debug("Action executor init");
        commands = new TreeMap<>();
        for (Field field : ActionCommands.class.getDeclaredFields()) {
            try {
                commands.put((String) field.get(null), ReflectionActionExecutor.class.getMethod((String) field.get(null)));
            } catch (Exception e) {
                log.warn("error reading command field {} ", field.getName());
            }
        }
    }

    @Override
    public void execute(String command) {
        log.debug("Execute " + command);
        ReflectionUtil.invokeMethod(commands.get(command), this);
    }

    public void exit() {
        System.exit(Constants.ZERO);
    }

    public void showShutdownTimeChooser() {
//        frame.showShutdownTimeChooser(); TODO
    }

    public void openQuickNavi() {
        new Thread(() -> {
            mediaService.pause();
            viewService.switchToQuickNavi();
            mediaService.updateCurrentMediaInCache();
            mediaService.stop();
            cacheService.save();
        }).start();
    }

    public void togglePlayer() {
        mediaService.toggle();
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

}
