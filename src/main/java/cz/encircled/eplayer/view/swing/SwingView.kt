package cz.encircled.eplayer.view.swing

import com.formdev.flatlaf.FlatDarculaLaf
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.TimeTracker
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiUtil
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.components.MainFrame
import org.apache.logging.log4j.LogManager
import java.awt.Font
import javax.swing.JFrame
import javax.swing.UIManager


fun main(args: Array<String>) {
    SwingView(args)
}

/**
 * TODO
 * - add media info
 * - add grouping in folder
 */
class SwingView(args: Array<String>) {

    private val log = LogManager.getLogger()
    private val core: ApplicationCore

    init {
        log.info("INPUT ARGS: ${args.joinToString()}")

        TimeTracker.tracking("UI base setup") {
            FlatDarculaLaf.install()
            JFrame.setDefaultLookAndFeelDecorated(true)

            UIManager.put("defaultFont", Font("Segoe UI", Font.PLAIN, 13));

        }
        core = ApplicationCore()

        TimeTracker.tracking("UI components") {
            val dataModel = UiDataModel()
            val quickNaviController = QuickNaviController(dataModel, core)
            val mainFrame = MainFrame(dataModel, quickNaviController, core)

            quickNaviController.init(mainFrame)
            mainFrame.isVisible = true

            UiUtil.inNormalThread {
                core.delayedInit(mainFrame, quickNaviController)
            }
        }

        if (args.isNotEmpty()) {
            Event.contextInitialized.listen {
                log.info("Play input video: ${args[0]}")
                it.mediaService.play(args[0])
            }
        }

    }

}