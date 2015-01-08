package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.elight.core.annotation.Component;
import cz.encircled.elight.core.annotation.Wired;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Component("quickNaviMediaTab")
public class QuickNaviMediaTab extends MediaTab {

    @Wired
    private CacheService cacheService;

    @PostConstruct
    private void initialize() {
        setText("Quick Navigation");
        setClosable(false);
    }

    @Override
    protected Collection<MediaType> getAllMediaTypes() {
        return cacheService.getCache();
    }

}
