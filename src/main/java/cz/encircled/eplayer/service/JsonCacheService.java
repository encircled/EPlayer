package cz.encircled.eplayer.service;

import com.google.gson.JsonSyntaxException;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.SettingsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static cz.encircled.eplayer.util.Localization.*;

/**
 * Created by Administrator on 9.6.2014.
 */
@Resource
public class JsonCacheService implements CacheService {

    public static final String QUICK_NAVI_PATH = Application.APP_DOCUMENTS_ROOT + "quicknavi2.json";

    private Map<String, MediaType> cache;

    private static final Logger log = LogManager.getLogger();

    @Resource
    private GuiUtil guiUtil;

    public JsonCacheService() {
        long start = System.currentTimeMillis();
        log.trace("JsonCacheService init start");
        if (IOUtil.createIfMissing(QUICK_NAVI_PATH, false, true)) {
            log.debug("QuickNavi file was created");
        }
        try {
            cache = IOUtil.getPlayableJson(QUICK_NAVI_PATH);
        } catch (IOException e) {
            log.error("Failed to read cache data from {} with default type token. Message: {}",
                    SettingsProvider.get(QUICK_NAVI_PATH), e.getMessage());
            guiUtil.showMessage(msgQnFileIoFail.ln(), errorTitle.ln());
        } catch (JsonSyntaxException e) {
            log.error("JSON syntax error. Message: {}", e.getMessage());
            guiUtil.showMessage(msgQnFileCorrupted.ln(), errorTitle.ln());
        }
        if (cache == null)
            cache = new HashMap<>();

        log.trace("JsonCacheService init complete in {} ms", System.currentTimeMillis() - start);
    }

    @Override
    public void forEach(Consumer<MediaType> action) {
        getCache().forEach(action);
    }

    @Override
    public MediaType createIfAbsent(@NotNull String path) {
        return cache.computeIfAbsent(path, p -> new MediaType(path));
    }

    @Override
    public MediaType addIfAbsent(@NotNull MediaType mediaType) {
        log.debug("Add if absent {}", mediaType.toString());
        return cache.putIfAbsent(mediaType.getPath(), mediaType);
    }

    @Override
    public MediaType getEntry(String id) {
        return cache.get(id);
    }

    @Override
    public MediaType updateEntry(String id, long time) {
        MediaType p = cache.get(id);
        log.debug("Update cache entry {}, time {}", p, time);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        return p;
    }

    @Override
    public MediaType deleteEntry(String id) {
        return cache.remove(id);
    }

    @NotNull
    @Override
    public List<MediaType> getCache() {
        return new ArrayList<>(cache.values());
    }

    @Nullable
    @Override
    public MediaType getLastByWatchDate() {
        MediaType p = getCache()
                .stream()
                .max((p1, p2) -> Long.compare(p1.getWatchDate(), p2.getWatchDate())).get();
        log.debug("Last played: {}", p);
        return p;
    }

    /**
     * Save playable map to file in JSON format
     */
    @Override
    public synchronized void save() {
        log.debug("Save json cache");
        try {
            IOUtil.storeJson(cache, QUICK_NAVI_PATH);
            log.debug("Json successfully saved");
        } catch (IOException e) {
            log.error("Failed to save playable to {}, msg {}", QUICK_NAVI_PATH, e);
        }
    }

}
