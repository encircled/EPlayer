package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource(name = "quickNaviMediaTab")
@Runner(FxRunner.class)
public class QuickNaviMediaTab extends MediaTab {

    @Resource
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
