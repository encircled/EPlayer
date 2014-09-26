package cz.encircled.eplayer.service.gui;

import cz.encircled.eplayer.model.MediaType;

/**
 * Created by Encircled on 14/09/2014.
 */
public interface FromGuiViewService {

    void play(MediaType media);

    void createNewTab(String absolutePath);

    void removeTabForFolder(String tabName);

    void deleteMedia(int hashCode);

}
