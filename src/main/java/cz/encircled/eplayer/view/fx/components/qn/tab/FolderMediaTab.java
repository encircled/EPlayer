package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.elight.core.annotation.Component;
import cz.encircled.elight.core.annotation.Scope;
import cz.encircled.elight.core.annotation.Wired;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.Settings;

import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Component
@Scope(Scope.PROTOTYPE)
public class FolderMediaTab extends MediaTab {

    @Wired
    private FolderScanService folderScanService;

    private String pathToFolder;

    public void setPath(String pathToFolder) {
        this.pathToFolder = pathToFolder;
        setText(pathToFolder);
        setOnClosed(event -> new Thread(() -> {
            Settings.folders_to_scan.removeFromList(pathToFolder).save();
        }).start());

    }

    @Override
    protected Collection<MediaType> getAllMediaTypes() {
        return folderScanService.getMediaInFolder(pathToFolder);
    }

}
