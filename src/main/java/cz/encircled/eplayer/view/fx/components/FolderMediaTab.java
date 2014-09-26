package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.core.SeriesFinder;
import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import cz.encircled.eplayer.service.FolderScanService;
import cz.encircled.eplayer.util.Settings;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public class FolderMediaTab extends MediaTab {

    @Resource
    private FolderScanService folderScanService;

    @Resource
    private Settings settings;

    private String pathToFolder;

    public void setPath(String pathToFolder) {
        this.pathToFolder = pathToFolder;
        setText(pathToFolder);
        setOnClosed(event -> new Thread(() -> {
            settings.removeFromList(Settings.FOLDERS_TO_SCAN, pathToFolder);
            settings.save();
        }));
    }

    @Resource
    private SeriesFinder seriesFinder;

    @Override
    protected Collection<MediaType> getMediaTypes() {
        Map<String, SeriesVideo> series = seriesFinder.findSeries(folderScanService.getMediaInFolder(pathToFolder));
        Collection<MediaType> res = new ArrayList<>(series.size());
        series.forEach((name, s) -> {
            res.add(s.getLast());
        });
        return res;

//        return folderScanService.getMediaInFolder(pathToFolder);
    }
}
