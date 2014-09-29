package cz.encircled.eplayer.service.gui;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 9.6.2014.
 */
public interface ViewService {

    boolean isPlayerState();

    void enterFullScreen();

    void exitFullScreen();

    void showPlayer(CountDownLatch countDownLatch);

    // TODO
    void enableSubtitlesMenu(boolean isEnabled);

    void switchToQuickNavi();

}
