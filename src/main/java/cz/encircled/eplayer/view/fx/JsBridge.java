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

import java.io.File;
import java.util.*;
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

            Comparator<MediaType> comparator;
            switch (uiState.getOrderBy()) {
                case SIZE:
                    comparator = (o1, o2) -> Long.compare(o1.getSize(), o2.getSize());
                    break;
                case CREATION_DATE:
                    comparator = (o1, o2) -> Long.compare(o1.getFileCreationDate(), o2.getFileCreationDate());
                    break;
                default:
                    comparator = (o1, o2) -> o1.getName().compareTo(o2.getName());
            }

            if (uiState.isReverseOrder()) {
                comparator = Collections.reverseOrder(comparator);
            }

            mediaInFolder.sort(comparator);

            pushToUi("showMediaCallback", uiState.getPath(), mediaInFolder);
        }).start();

    }

    public String getMediaTabs() {
        log.debug("GetMediaTabs call");
        List<TabDto> tabs = Settings.folders_to_scan.getList().stream().map(path -> new TabDto(path, true)).collect(Collectors.toList());
        tabs.add(0, new TabDto("QuickNavi", false));
        return toJson(tabs);
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

    public void removeMedia(String path, boolean shouldDeleteFile) {
        log.debug("Remove media call: {}", path);
        new Thread(() -> {
            core.getCacheService().deleteEntry(path);
            if (shouldDeleteFile) {
                if (!new File(path).delete()) {
                    // TODO log
                }
            }
        }).start();
    }

    public void closeTab(String path) {
        log.debug("Close tab call: {}", path);
        new Thread(() -> {
            Settings.folders_to_scan.removeFromList(path);
            Settings.folders_to_scan.save();
        }).start();
    }

    // State callbacks

    public void onFilterUpdate(String newValue) {
        if (newValue == null) {
            newValue = "";
        }
        if (!newValue.equals(uiState.getFilter())) {
            uiState.setFilter(newValue);
            refreshCurrentTab();
        }
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

    public void onOrderByUpdate(String orderBy) {
        if (StringUtil.isSet(orderBy)) {
            uiState.setOrderBy(UiState.OrderBy.valueOf(orderBy));
            refreshCurrentTab();
        }
    }

    public void onReverseOrderUpdate(boolean isReverseOrder) {
        uiState.setIsReverseOrder(isReverseOrder);
        refreshCurrentTab();
    }

    // State callbacks end

    public void pushToUi(String action, Object... param) {
        log.debug("pushToUi call: {}", action);
        Platform.runLater(() -> {
            if (param.length == 1) {
                windowObject.call(action, toJson(param[0]));
            } else {
                windowObject.call(action, toJson(param));
            }
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

    public static class TabDto {

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
