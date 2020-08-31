package cz.encircled.eplayer.view;

import java.util.concurrent.CountDownLatch;

/**
 * @author Encircled on 16/09/2014.
 */
public interface AppView {

    String TITLE = "EPlayer";

    void showPlayer(CountDownLatch countDownLatch);

    boolean isPlayerScene();

    boolean isFullScreen();

    void showQuickNavi();

    void toggleFullScreen();

    void openMediaChooser();

}
