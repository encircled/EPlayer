package cz.encircled.eplayer.service.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReflectionActionExecutor {

    private final static Logger log = LogManager.getLogger();


    public void showShutdownTimeChooser() {
//        frame.showShutdownTimeChooser(); TODO
    }

    public void back() {
        log.debug("Execute back");
//        if (viewService.isPlayerState()) {
//            if (mediaService.isFullScreen()) {
//                mediaService.exitFullScreen();
//                mediaService.updateCurrentMediaInCache();
//            } else
//                execute(ActionCommands.openQuickNavi);
//        }
    }

}
