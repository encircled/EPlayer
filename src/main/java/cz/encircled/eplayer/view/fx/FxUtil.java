package cz.encircled.eplayer.view.fx;

/**
 * @author Encircled on 20/09/2014.
 */
public class FxUtil {

    public static void workInNormalThread(Runnable runnable) {
        new Thread(runnable).start();
    }

}
