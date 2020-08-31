package cz.encircled.eplayer.view.fx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaTab;
import cz.encircled.eplayer.model.PlayableMedia;
import cz.encircled.eplayer.remote.RemoteControlHandler;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.util.StringUtil;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Kisel on 13.08.2015.
 */
public class JsBridge implements RemoteControlHandler {

    private static final Logger log = LogManager.getLogger();
    private static final Pattern seriesPattern = Pattern.compile("(?i).*s[\\d]{1,2}.?e[\\d]{1,2}.*");
    private final UiState uiState;
    private final ApplicationCore core;
    private final JSObject windowObject;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public JsBridge(ApplicationCore core, JSObject windowObject) {
        this.core = core;
        this.windowObject = windowObject;
        this.uiState = new UiState();
    }

    // REMOTE CONTROL

    @Override
    public void toFullScreen() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void back() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void goToNextMedia() {
        if (uiState.currentMedia != null) {
            if (uiState.selectedItem == null || uiState.selectedItem == uiState.currentMedia.size() - 1) {
                uiState.selectedItem = 0;
            } else {
                uiState.selectedItem++;
            }
            refreshCurrentTab(uiState.currentMedia);
        }
    }

    @Override
    public void goToPrevMedia() {
        if (uiState.currentMedia != null) {
            if (uiState.selectedItem == null || uiState.selectedItem == 0) {
                uiState.selectedItem = 0;
            } else {
                uiState.selectedItem--;
            }
            refreshCurrentTab(uiState.currentMedia);
        }
    }

    @Override
    public void playSelected() {
        if (isValidSelectedItem()) {
            playMedia(uiState.currentMedia.get(uiState.selectedItem).mediaFile().getPath());
        }
    }

    @Override
    public void watchLastMedia() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void playPause() {
        throw new RuntimeException("Not supported");
    }


    // APP LOGIC
    public void refreshCurrentTab() {
        new Thread(() -> {
            Matcher matcher = seriesPattern.matcher("");
            List<PlayableMedia> mediaInFolder = uiState.isQuickNavi() ? core.getCacheService().getCache() : core.getFolderScanService().getMediaInFolder(uiState.getPath());

            mediaInFolder = getFilteredMedia(uiState.getFilter(), mediaInFolder);

            switch (uiState.getViewType()) {
                case SERIES:
                    mediaInFolder = mediaInFolder.stream().filter((m) -> matcher.reset(m.mediaFile().getName()).matches()).collect(Collectors.toList());
                    break;
                case FILMS:
                    mediaInFolder = mediaInFolder.stream().filter((m) -> !matcher.reset(m.mediaFile().getName()).matches()).collect(Collectors.toList());
                    break;
            }

/*          TODO  Comparator<PlayableMedia> comparator;
            switch (uiState.getOrderBy()) {
                case SIZE:
                    comparator = Comparator.comparingLong(MediaFile::getSize);
                    break;
                case CREATION_DATE:
                    comparator = Comparator.comparingLong(MediaFile::getFileCreationDate);
                    break;
                default:
                    comparator = Comparator.comparing(MediaFile::getName);
            }

            if (uiState.isReverseOrder()) {
                comparator = Collections.reverseOrder(comparator);
            }

            mediaInFolder.sort(comparator);*/

            refreshCurrentTab(mediaInFolder);
        }).start();
    }

    private boolean isValidSelectedItem() {
        return uiState.currentMedia != null && uiState.selectedItem != null && uiState.selectedItem < uiState.currentMedia.size();
    }

    private void refreshCurrentTab(List<PlayableMedia> media) {
        uiState.currentMedia = media;
        pushToUi("showMediaCallback", uiState.getPath(), media, uiState.selectedItem);
    }

    public String getMediaTabs() {
        List<MediaTab> tabs = core.getSettings().getFoldersToScan().stream()
                .map(path -> new MediaTab(System.currentTimeMillis(), path, true))
                .collect(Collectors.toList());

        tabs.add(0, new MediaTab(0, "QuickNavi", false));
        try {
            String result = toJson(tabs);
            log.debug("GetMediaTabs call: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getLocalization() throws JsonProcessingException {
        Map<String, String> map = Arrays.stream(Localization.values()).collect(Collectors.toMap(Localization::name, Localization::ln));
        return toJson(map);
    }

    public void playMedia(String path) {
        log.debug("Play media call: {}", path);
        new Thread(() -> core.getMediaService().play(path)).start();
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
        new Thread(() -> core.getSettings().removeFolderToScan(path)).start();
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
            try {
                if (param.length == 1) {
                    windowObject.call(action, toJson(param[0]));
                } else {
                    windowObject.call(action, toJson(param));
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace(); // TODO
            }
        });
    }

    public void log(String text) {
        System.out.println("JS: " + text);
    }

    private List<PlayableMedia> getFilteredMedia(String filter, List<PlayableMedia> mediaFiles) {
        if (StringUtil.isNotBlank(filter)) {
            Pattern p = Pattern.compile("(?i).*" + filter.replaceAll(" ", ".*") + ".*");
            Matcher m = p.matcher("");
            return mediaFiles.stream().filter(media -> m.reset(media.mediaFile().getName()).matches()).collect(Collectors.toList());
        } else {
            return mediaFiles;
        }
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

}
