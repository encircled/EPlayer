package cz.encircled.eplayer.service;

/**
 * Created by Encircled on 9/06/2014.
 */
public interface ActionExecutor {


    void setViewService(ViewService viewService);

    void setMediaService(MediaService mediaService);

    void setCacheService(CacheService cacheService);

    void execute(String command);

}
