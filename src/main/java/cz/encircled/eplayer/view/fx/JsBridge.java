package cz.encircled.eplayer.view.fx;

import com.google.gson.Gson;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.util.Settings;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Kisel on 13.08.2015.
 */
public class JsBridge {

    private static final Logger log = LogManager.getLogger();

    private ApplicationCore core;

    private JSObject windowObject;

    public JsBridge(ApplicationCore core, JSObject windowObject) {
        this.core = core;
        this.windowObject = windowObject;
    }

    public String getMediaTabs() {
        log.debug("GetMediaTabs call");
        List<TabDto> tabs = Settings.folders_to_scan.getList().stream().map(path -> new TabDto(path, true)).collect(Collectors.toList());
        return toJson(tabs);
    }

    public void getMediaTabContent(String path) {
        log.debug("getMediaTabContent call");
        new Thread(() -> {
            List<MediaType> mediaInFolder = core.getFolderScanService().getMediaInFolder(path);
            pushToUi("showMediaCallback", path, mediaInFolder);
        }).start();
    }

    public void getQuickNaviContent() {
        log.debug("getQuickNaviContent call");
        new Thread(() -> {
            List<MediaType> mediaInFolder = core.getCacheService().getCache();
            pushToUi("showQuickNaviCallback", mediaInFolder);
        }).start();
    }

    public String getLocalization() {
        Map<String, String> map = Arrays.stream(Localization.values()).collect(Collectors.toMap(Localization::name, Localization::ln));
        return toJson(map);
    }

    public void playMedia(String path) {
        log.debug("Play media call: {}", path);
        new Thread(() -> {
            core.getMediaService().play(path);
        }).start();
    }

    public void pushRefreshCurrentTab() {
        pushToUi("refreshCurrentTabCallback");
    }

    public void pushToUi(String action, Object... param) {
        log.debug("pushToUi call: {}", action);
        Platform.runLater(() -> {
            windowObject.call(action, toJson(param));
        });
    }

    private String toJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public class TabDto {

        long id;

        String path;

        boolean closeable = true;

        public TabDto(String path, boolean closeable) {
            this.id = System.nanoTime();
            this.path = path;
            this.closeable = closeable;
        }
    }

}
