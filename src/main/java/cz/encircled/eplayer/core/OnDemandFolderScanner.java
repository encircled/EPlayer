package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.IOUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Encircled on 20/09/2014.
 */
public class OnDemandFolderScanner implements FolderScanService {

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("avi", "mkv", "mp3", "mp4", "flv", "wav", "wmv", "mov");

    private ApplicationCore core;

    public OnDemandFolderScanner(@NotNull ApplicationCore core) {
        this.core = core;
    }

    @Override
    @NotNull
    public List<MediaType> getMediaInFolder(@NotNull String path) {
        List<MediaType> mediaTypes = new ArrayList<>();
        IOUtil.getFilesInFolder(path).forEach(file -> {
            if (SUPPORTED_FORMATS.contains(FilenameUtils.getExtension(file.getName()))) {
                MediaType entry = core.getCacheService().getEntry(file.getPath());
                if (entry == null) {
                    entry = new MediaType(file.getPath());
                }
                mediaTypes.add(entry);
            }
        });
        return mediaTypes;
    }

}
