package cz.encircled.eplayer.view.fx.components.qn.tab;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Encircled on 20/09/2014.
 */
public class QuickNaviMediaTab extends MediaTab {

    ApplicationCore core;

    public QuickNaviMediaTab(ApplicationCore core, QuickNaviScreen screen) {
        super(core, screen);
        this.core = core;
        setText("Quick Navigation");
        setClosable(false);
    }

    @NotNull
    @Override
    protected Collection<MediaType> getAllMediaTypes() {
        return core.getCacheService().getCache();
    }

}
