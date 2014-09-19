package cz.encircled.eplayer.view.dnd;

import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.gui.FromGuiViewService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * Created by Encircled on 11/09/2014.
 */
@Resource
public class DndHandler {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private FromGuiViewService fromGuiViewService;

    @Resource
    private MediaService mediaService;

    @Resource
    private CacheService cacheService;

    public void receive(List<File> files) {
        files.stream().forEach(file -> {
            log.debug("DnD file path {}", file.getAbsolutePath());
            if (file.exists()) {
                if (file.isDirectory()) {
                    fromGuiViewService.createNewTab(file.getAbsolutePath());
                } else {
                    if (files.size() > 1) {
                        cacheService.createIfAbsent(file.getPath());
                    } else {
                        mediaService.play(file.getPath());
                    }
                }
            }
        });
    }

}
