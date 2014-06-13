package cz.encircled.eplayer.service;

import com.google.gson.JsonSyntaxException;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.PropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static cz.encircled.eplayer.util.GUIUtil.showMessage;
import static cz.encircled.eplayer.util.LocalizedMessages.*;

/**
 * Created by Administrator on 9.6.2014.
 */
public class JsonCacheService implements CacheService {

    public static final String QUICK_NAVI_PATH = Application.APP_DOCUMENTS_ROOT + "quicknavi.json";

    private Map<Integer, MediaType> cache;

    private static final Logger log = LogManager.getLogger();


    @Override
    public void forEach(Consumer<MediaType> action) {
        getCache().forEach(action);
    }

    @Override
    public MediaType createIfAbsent(@NotNull String path){
        return cache.computeIfAbsent(path.hashCode(), hash -> new MediaType(path));
    }

    @Override
    public MediaType getEntry(Integer hashCode){
        return cache.get(hashCode);
    }

    @Override
    public MediaType updateEntry(int hash, long time){
        MediaType p = cache.get(hash);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        return p;
    }

    @Override
    public MediaType deleteEntry(int hash){
        return cache.remove(hash);
    }

    // TODO
//    public void deletePlayableOld(int hash){
//        Playable deleted = cache.remove(hash);
//        if(deleted == null){
//            log.warn("Playable with hash {} not exists", hash);
//        } else {
//            if (deleted.exists() && userConfirm(CONFIRM_DELETE_FILE)){
//                boolean wasDeleted =     new File(deleted.getPath()).delete();
//                log.debug("Playable {} was deleted: {}", deleted.getName() ,wasDeleted);
//            }
//            actionExecutor.setDefaultFileChooserPath();
//            frame.repaintQuickNavi(); // TODO
//            savePlayable();
//        }
//    }

    @NotNull
    @Override
    public Collection<MediaType> getCache(){
        return cache.values();
    }

    @Nullable
    @Override
    public MediaType getLastByWatchDate(){
        MediaType p = getCache()
                .stream()
                .max((p1, p2) -> Long.compare(p1.getWatchDate(), p2.getWatchDate())).get();
        log.debug("Last played: {}", p);
        return p;
    }

    /**
     * Async save playable map to file in JSON format
     */
    @Override
    public synchronized void save(){
        new Thread(() -> {
            try {
                IOUtil.storeJson(cache, QUICK_NAVI_PATH);
                log.debug("Json successfully saved");
            } catch (IOException e) {
                log.error("Failed to save playable to {}, msg {}", QUICK_NAVI_PATH, e);
            }
        }).start();
    }

    public void initialize(@NotNull CountDownLatch countDownLatch){
        long start = System.currentTimeMillis();
        log.trace("JsonCacheService init start");
        try {
            if(IOUtil.createIfMissing(QUICK_NAVI_PATH)){
                log.debug("QuickNavi file was created");
            }
        } catch (IOException e) {
            log.error("Failed to create QuickNavi data file at {}", QUICK_NAVI_PATH);
            showMessage(MSG_CREATE_QN_FILE_FAIL, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            cache = IOUtil.getPlayableJson(QUICK_NAVI_PATH);
        } catch (IOException e) {
            log.error("Failed to read cache data from {} with default type token. Message: {}",
                    PropertyProvider.get(QUICK_NAVI_PATH), e.getMessage());
            showMessage(MSG_QN_FILE_IO_FAIL, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        } catch (JsonSyntaxException e){
            log.error("JSON syntax error. Message: {}", e.getMessage());
            showMessage(MSG_QN_FILE_CORRUPTED, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
        if(cache == null)
            cache = new HashMap<>();

        checkHashes(cache);
        log.trace("JsonCacheService init complete in {} ms", System.currentTimeMillis() - start);
        countDownLatch.countDown();
    }

    private static void checkHashes(@NotNull Map<Integer, MediaType> playableCache) {
        List<Integer> corruptedHashes = new ArrayList<>();
        playableCache.forEach((key, media) -> {
            if (media.hashCode() != key) {
                corruptedHashes.add(key);
                log.warn("Playable {} has wrong hash code, updating...", media.getName());
            }
        });
        corruptedHashes.forEach((oldHash) -> playableCache.put(playableCache.get(oldHash).hashCode(), playableCache.remove(oldHash)));
    }

}
