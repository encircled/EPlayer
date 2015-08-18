package cz.encircled.eplayer.view.fx;

import com.google.gson.Gson;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.util.StringUtil;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Kisel on 13.08.2015.
 */
public class JsBridge {

    private static final Logger log = LogManager.getLogger();
    private static final Pattern seriesPattern = Pattern.compile("(?i).*s[\\d]{1,2}.?e[\\d]{1,2}.*");
    private UiState uiState;
    private ApplicationCore core;
    private JSObject windowObject;

    public JsBridge(ApplicationCore core, JSObject windowObject) {
        this.core = core;
        this.windowObject = windowObject;
        this.uiState = new UiState();
    }

    public void refreshCurrentTab() {
        new Thread(() -> {
            Matcher matcher = seriesPattern.matcher("");
            List<MediaType> mediaInFolder = uiState.isQuickNavi() ? core.getCacheService().getCache() : core.getFolderScanService().getMediaInFolder(uiState.getPath());

            mediaInFolder = getFilteredMedia(uiState.getFilter(), mediaInFolder);

            switch (uiState.getViewType()) {
                case SERIES:
                    mediaInFolder = mediaInFolder.stream().filter((m) -> matcher.reset(m.getName()).matches()).collect(Collectors.toList());
                    break;
                case FILMS:
                    mediaInFolder = mediaInFolder.stream().filter((m) -> !matcher.reset(m.getName()).matches()).collect(Collectors.toList());
                    break;
            }

            pushToUi("showMediaCallback", uiState.getPath(), mediaInFolder);
        }).start();

    }

    public String getMediaTabs() {
        log.debug("GetMediaTabs call");
        List<TabDto> tabs = Settings.folders_to_scan.getList().stream().map(path -> new TabDto(path, true)).collect(Collectors.toList());
        return toJson(tabs);
    }

    public void getMediaTabContent(String path) {
        log.debug("getMediaTabContent call, path {}", path);
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

    public void filterQuickNavi(String filter) {
        log.debug("filterQuickNavi: {}", filter);
        new Thread(() -> {
            List<MediaType> mediaTypes = core.getCacheService().getCache();
            pushToUi("showQuickNaviCallback", getFilteredMedia(filter, mediaTypes));
        }).start();
    }

    public void filter(String path, String filter) {
        log.debug("filter: {}, tab {}", filter, path);
        new Thread(() -> {
            List<MediaType> mediaTypes = core.getFolderScanService().getMediaInFolder(path);
            pushToUi("showMediaCallback", path, getFilteredMedia(filter, mediaTypes));
        }).start();
    }

    // State callbacks

    public void onFilterUpdate(String newValue) {
        uiState.setFilter(newValue);
        refreshCurrentTab();
    }

    public void onTabUpdate(String newTabPath) {
        uiState.setPath(newTabPath);
        uiState.setIsQuickNavi("QuickNavi".equals(newTabPath));
        refreshCurrentTab();
    }

    public void onViewTypeUpdate(String newViewType) {
        if (StringUtil.isSet(newViewType)) {
            uiState.setViewType(UiState.ViewType.valueOf(newViewType));
            refreshCurrentTab();
        }
    }

    public void pushToUi(String action, Object... param) {
        log.debug("pushToUi call: {}", action);
        Platform.runLater(() -> {
            windowObject.call(action, toJson(param));
        });
    }

    private List<MediaType> getFilteredMedia(String filter, List<MediaType> mediaTypes) {
        if (StringUtil.isNotBlank(filter)) {
            Pattern p = Pattern.compile("(?i).*" + filter.replaceAll(" ", ".*") + ".*");
            Matcher m = p.matcher("");
            return mediaTypes.stream().filter(media -> m.reset(media.getName()).matches()).collect(Collectors.toList());
        } else {
            return mediaTypes;
        }
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
