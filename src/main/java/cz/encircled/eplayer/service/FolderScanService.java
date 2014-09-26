package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;

import java.util.List;

/**
 * Created by Encircled on 11/06/2014.
 */
public interface FolderScanService {

    List<MediaType> getMediaInFolder(String pathToFolder);

}
