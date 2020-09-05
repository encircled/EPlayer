package cz.encircled.eplayer.view.fx

import javafx.application.Platform
import javafx.scene.control.ScrollPane

/**
 * @author Encircled on 20/09/2014.
 */
object FxUtil {

    fun inNormalThread(runnable: Runnable) {
        if (!Platform.isFxApplicationThread()) {
            runnable.run()
        } else {
            Thread(runnable).start()
        }
    }

    fun withFastScroll(pane: ScrollPane): ScrollPane =
            pane.apply {
                content.setOnScroll { scrollEvent ->
                    val deltaY: Double = scrollEvent.deltaY * 0.01
                    pane.vvalue = pane.vvalue - deltaY
                }
            }

}