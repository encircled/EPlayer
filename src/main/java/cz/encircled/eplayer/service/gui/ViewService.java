package cz.encircled.eplayer.service.gui;

import java.util.concurrent.CountDownLatch;

/**
 * @author Encircled on 9.6.2014.
 */
public interface ViewService {

    void showPlayer(CountDownLatch countDownLatch);

    // TODO
    void enableSubtitlesMenu(boolean isEnabled);

    void switchToQuickNavi();

}
