package cz.encircled.eplayer.view.fx;

import com.google.gson.Gson;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.Settings;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kisel on 13.08.2015.
 */
public class JsBridge {

    private static final Logger log = LogManager.getLogger();

    private FolderScanService folderScanService;

    private JSObject windowObject;

    public JsBridge(FolderScanService folderScanService, JSObject windowObject) {
        this.folderScanService = folderScanService;
        this.windowObject = windowObject;
    }

    public String getMediaTabs() {
        log.debug("GetMediaTabs call");
        List<TabDto> tabs = Settings.folders_to_scan.getList().stream().map(path -> new TabDto(path, true)).collect(Collectors.toList());
        return new Gson().toJson(tabs);
    }

    public void getMediaTabContent(String path) {
        log.debug("getMediaTabContent call");
        new Thread(() -> {
            List<MediaType> mediaInFolder = folderScanService.getMediaInFolder("D:/video");
            pushToUi("showMediaCallback", path, mediaInFolder);
        }).start();
    }

    public void pushToUi(String action, Object... param) {
        log.debug("pushToUi call: {}", action);
        Platform.runLater(() -> {
            windowObject.call(action, new Gson().toJson(param));
        });
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
