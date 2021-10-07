package cz.encircled.eplayer.view.swing

import com.formdev.flatlaf.FlatDarculaLaf
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.TimeMeasure.measure
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiUtil
import cz.encircled.eplayer.view.controller.PlayerControllerImpl
import cz.encircled.eplayer.view.controller.QuickNaviControllerImpl
import cz.encircled.eplayer.view.swing.components.MainFrame
import org.apache.logging.log4j.LogManager
import java.awt.Font
import javax.swing.JFrame
import javax.swing.UIManager


fun main(args: Array<String>) {
    SwingView(args)
}

class SwingView(args: Array<String>) {

    private val log = LogManager.getLogger()
    private val core: ApplicationCore

    init {
        log.info("INPUT ARGS: ${args.joinToString()}")

        measure("UI base setup") {
            FlatDarculaLaf.setup()
            JFrame.setDefaultLookAndFeelDecorated(true)

            UIManager.put("defaultFont", Font("Segoe UI", Font.PLAIN, 13));
        }

        core = ApplicationCore()

        measure("UI components") {
            val dataModel = UiDataModel()
            val quickNaviController = QuickNaviControllerImpl(dataModel, core)
            val playerController = PlayerControllerImpl(core)
            val mainFrame = MainFrame(dataModel, quickNaviController, playerController, core)

            quickNaviController.init(mainFrame)
            mainFrame.isVisible = true

            UiUtil.inNormalThread {
                core.delayedInit(mainFrame)
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