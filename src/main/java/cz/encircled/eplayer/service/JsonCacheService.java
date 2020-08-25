package cz.encircled.eplayer.service;

import com.google.gson.JsonSyntaxException;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.SettingsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author Encircled on 9.6.2014.
 */
public class JsonCacheService implements CacheService {

    public static final String QUICK_NAVI_PATH = ApplicationCore.APP_DOCUMENTS_ROOT + "quicknavi2.json";
    private static final Logger log = LogManager.getLogger();
    private Map<String, MediaType> cache = new HashMap<>();

    public void init() {
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
            // TODO
//            guiUtil.showMessage(msgQnFileIoFail.ln(), errorTitle.ln());
        } catch (JsonSyntaxException e) {
            log.error("JSON syntax error. Message: {}", e.getMessage());
            // TODO
//            guiUtil.showMessage(msgQnFileCorrupted.ln(), errorTitle.ln());
        }
        if (cache == null) {
            cache = new HashMap<>();
        }

        log.trace("JsonCacheService init complete in {} ms", System.currentTimeMillis() - start);
    }

    @Override
    public void forEach(@NotNull Consumer<MediaType> action) {
        getCache().forEach(action);
    }

    @NotNull
    @Override
    public MediaType createIfAbsent(@NotNull String path) {
        return createIfAbsent(path, null);
    }

    @NotNull
    @Override
    public MediaType createIfAbsent(@NotNull String path, @Nullable String title) {
        return cache.computeIfAbsent(path, p -> new MediaType(path, title));
    }

    @NotNull
    @Override
    public MediaType addIfAbsent(@NotNull MediaType mediaType) {
        log.debug("Add if absent {}", mediaType.toString());
        return cache.putIfAbsent(mediaType.getPath(), mediaType);
    }

    @Override
    @Nullable
    public MediaType getEntry(@NotNull String id) {
        return cache.get(id);
    }

    @Override
    @Nullable
    public MediaType updateEntry(@NotNull String id, long time) {
        MediaType p = cache.get(id);
        log.debug("Update cache entry {}, time {}", p, time);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        return p;
    }

    @Override
    @Nullable
    public MediaType deleteEntry(@NotNull String id) {
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
                .max(Comparator.comparingLong(MediaType::getWatchDate)).get();
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
