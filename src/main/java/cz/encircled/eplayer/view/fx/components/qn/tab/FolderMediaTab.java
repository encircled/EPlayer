package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.Settings;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public class FolderMediaTab extends MediaTab {

    @Resource
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
