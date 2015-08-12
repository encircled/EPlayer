package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Encircled on 20/09/2014.
 */
public class FolderMediaTab extends MediaTab {

    private String pathToFolder;

    public FolderMediaTab(ApplicationCore core, QuickNaviScreen quickNaviScreen) {
        super(core, quickNaviScreen);
    }

    public void setPath(String pathToFolder) {
        this.pathToFolder = pathToFolder;
        setText(pathToFolder);
        setOnClosed(event -> new Thread(() -> {
            Settings.folders_to_scan.removeFromList(pathToFolder).save();
        }).start());

    }

    @NotNull
    @Override
    protected Collection<MediaType> getAllMediaTypes() {
        return core.getFolderScanService().getMediaInFolder(pathToFolder);
    }

}
