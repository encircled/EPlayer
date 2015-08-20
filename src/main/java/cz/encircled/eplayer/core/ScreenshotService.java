package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;

import java.io.File;

/**
 * @author Kisel on 20.08.2015.
 */
public interface ScreenshotService {

    File getScreenshot(MediaType mediaType);

    boolean storeScreenshot(File image, String path);

}
