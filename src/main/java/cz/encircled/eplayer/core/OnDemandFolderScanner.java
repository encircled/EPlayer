package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaFile;
import cz.encircled.eplayer.model.PlayableMedia;
import cz.encircled.eplayer.service.FolderScanService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Encircled on 20/09/2014.
 */
public class OnDemandFolderScanner implements FolderScanService {

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("avi", "mkv", "mp3", "mp4", "flv", "wav", "wmv", "mov");

    private final ApplicationCore core;

    public OnDemandFolderScanner(@NotNull ApplicationCore core) {
        this.core = core;
    }

    @Override
    @NotNull
    public List<PlayableMedia> getMediaInFolder(@NotNull String path) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        /*IOUtil.getFilesInFolder(path).forEach(file -> {
            if (SUPPORTED_FORMATS.contains(FilenameUtils.getExtension(file.getName()))) {
                PlayableMedia entry = core.getCacheService().getEntry(file.getPath());
                if (entry == null) {
                    entry = new MediaFile(file.getPath());
                }
                mediaFiles.add(entry);
            }
        }); TODO */
        return new ArrayList<>();
    }

}
