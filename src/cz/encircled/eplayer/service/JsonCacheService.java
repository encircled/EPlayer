package cz.encircled.eplayer.service;

import com.google.gson.JsonSyntaxException;
import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.IOUtil;
import cz.encircled.eplayer.util.PropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static cz.encircled.eplayer.util.GUIUtil.*;
import static cz.encircled.eplayer.util.LocalizedMessages.*;
import static cz.encircled.eplayer.util.LocalizedMessages.ERROR_TITLE;
import static cz.encircled.eplayer.util.LocalizedMessages.MSG_QN_FILE_CORRUPTED;

/**
 * Created by Administrator on 9.6.2014.
 */
public class JsonCacheService implements CacheService {

    public static final String QUICK_NAVI_PATH = Application.APP_DOCUMENTS_ROOT + "quicknavi.json";

    private Map<Integer, Playable> cache;

    private static final Logger log = LogManager.getLogger();


    @Override
    public Playable createIfAbsent(@NotNull String path){
        return cache.computeIfAbsent(path.hashCode(), hash -> new Playable(path));
    }

    @Override
    public Playable getEntry(Integer hashCode){
        return cache.get(hashCode);
    }

    @Override
    public CacheService updateEntry(int hash, long time){
        Playable p = cache.get(hash);
        p.setTime(time);
        p.setWatchDate(new Date().getTime());
        return this;// TODO save!
    }

    @Override
    public Playable deleteEntry(int hash){
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

    @Override
    public Collection<Playable> getCache(){
        return cache.values();
    }

    @Nullable
    private Playable getLastByWatchDate(){
        Playable p = getCache()
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

    private void initialize(){
        new Thread(() -> {
            log.trace("Init playable cache");
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
        }).start();
    }

    private static void checkHashes(Map<Integer, Playable> playableCache) {
        List<Integer> corruptedHashes = new ArrayList<>();
        playableCache.forEach((key, value) -> {
            if (value.getPath().hashCode() != key) {
                corruptedHashes.add(key);
                log.warn("Playable {} has wrong hash code, updating...", value.getName());
            }
        });
        corruptedHashes.forEach((oldHash) -> playableCache.put(playableCache.get(oldHash).getPath().hashCode(), playableCache.remove(oldHash)));
    }

}