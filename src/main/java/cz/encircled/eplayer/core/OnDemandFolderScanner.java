package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.FolderScanService;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
public class OnDemandFolderScanner implements FolderScanService {

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("avi", "mkv", "mp3", "wav", "wmv");

    public static final String DOT = ".";

    @Resource
    private CacheService cacheService;

    @Override
    public List<MediaType> getMediaInFolder(String path) {
        List<MediaType> mediaTypes = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    int lastDot = file.getName().lastIndexOf(DOT);
                    if (lastDot > -1 && SUPPORTED_FORMATS.contains(file.getName().toLowerCase().substring(lastDot + 1, file.getName().length()))) {
                        MediaType entry = cacheService.getEntry(file.getAbsolutePath().hashCode());
                        if (entry == null)
                            entry = new MediaType(file.getAbsolutePath());
                        mediaTypes.add(entry);
                    }
                } else {
                    mediaTypes.addAll((getMediaInFolder(file.getAbsolutePath())));
                }
            }
        }
        return mediaTypes;
    }

}
