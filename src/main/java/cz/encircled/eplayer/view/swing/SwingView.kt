package cz.encircled.eplayer.view.swing

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.ui.JBRCustomDecorations
import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.view.SwingEUiExecutor
import cz.encircled.eplayer.view.UiDataModel
import cz.encircled.eplayer.view.UiUtil
import cz.encircled.eplayer.view.controller.QuickNaviController
import cz.encircled.eplayer.view.swing.components.MainFrame
import org.apache.logging.log4j.LogManager
import java.awt.Font
import javax.swing.JFrame
import javax.swing.UIManager


fun main() {
    SwingView()
}

class SwingView {

    private val log = LogManager.getLogger()

    private val core: ApplicationCore

    init {
        val start = System.currentTimeMillis()
        FlatDarculaLaf.install()
        JFrame.setDefaultLookAndFeelDecorated(true)

        UIManager.put("defaultFont", Font("Segoe UI", Font.PLAIN, 13));

        UiUtil.uiExecutor = SwingEUiExecutor()
        core = ApplicationCore()

        val dataModel = UiDataModel()
        val quickNaviController = QuickNaviController(dataModel, core)
        val mainFrame = MainFrame(dataModel, quickNaviController, core)
        quickNaviController.init(mainFrame)
        mainFrame.isVisible = true

        log.debug("UI start finished in ${System.currentTimeMillis() - start}")

        Thread {
            core.delayedInit(mainFrame, quickNaviController)
        }.start()
    }

}