package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.IOUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("avi", "mkv", "mp3", "wav", "wmv", "mov");

    @Resource
    private CacheService cacheService;

    @Override
    public List<MediaType> getMediaInFolder(String path) {
        List<MediaType> mediaTypes = new ArrayList<>();
        IOUtil.getFilesInFolder(path).stream().forEach(file -> {
            if (SUPPORTED_FORMATS.contains(FilenameUtils.getExtension(file.getName()))) {
                MediaType entry = cacheService.getEntry(file.getAbsolutePath());
                if (entry == null)
                    entry = new MediaType(file.getAbsolutePath());
                mediaTypes.add(entry);
            }
        });
        return mediaTypes;
    }

}
