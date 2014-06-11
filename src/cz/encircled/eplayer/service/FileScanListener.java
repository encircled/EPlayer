package cz.encircled.eplayer.service;

import cz.encircled.eplayer.model.MediaType;

import java.util.Map;

/**
 * Created by Encircled on 11/06/2014.
 */
public abstract class FileScanListener {

    public void onFolderScanned(String path, Map<Integer, MediaType> media){

    }

    public void onFolderChange(String path, Map<Integer, MediaType> media){

    }

}
