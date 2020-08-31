package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.PlayableMedia;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Encircled on 11/06/2014.
 */
public interface FolderScanService {

    @NotNull
    List<PlayableMedia> getMediaInFolder(String pathToFolder);

}
