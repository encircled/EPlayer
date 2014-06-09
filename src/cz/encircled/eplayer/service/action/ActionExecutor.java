package cz.encircled.eplayer.service.action;

import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;

/**
 * Created by Encircled on 9/06/2014.
 */
public interface ActionExecutor {


    void setViewService(ViewService viewService);

    void setMediaService(MediaService mediaService);

    void setCacheService(CacheService cacheService);

    void execute(String command);

}
