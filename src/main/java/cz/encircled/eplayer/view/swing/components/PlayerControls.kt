package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.core.ApplicationCore
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.StringUtil
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.swing.*
import cz.encircled.eplayer.view.swing.components.base.BaseJPanel
import cz.encircled.eplayer.view.swing.components.base.ToggleButton
import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.JLabel
import javax.swing.JSlider

class PlayerControls(appView: AppView, val core: ApplicationCore) : BaseJPanel() {

    private val playerToggle: ToggleButton = ToggleButton("pause.png", "play.png")

    private val volumeToggle: ToggleButton = ToggleButton("volume.png", "volume_mute.png")
    private val volumeSlider: JSlider = JSlider(0, core.settings.maxVolume, core.settings.lastVolume)
    private val volumeText: JLabel = JLabel("${core.settings.lastVolume} %")
    private var lastVolumeSliderValue = 0

    private val fullScreenToggle: ToggleButton = ToggleButton("fit.png", "shrink.png")

    private val timeSlider: JSlider = JSlider()
    private val currentTimeText: JLabel = JLabel(StringUtil.msToTimeLabel(0L))
    private val totalTimeText: JLabel = JLabel(StringUtil.msToTimeLabel(0L))

    private val bgColor = Color(40, 42, 42)

    init {
        padding(20, 15)
        background = bgColor
        preferredSize = Dimension(AppView.MIN_WIDTH, 60)

        playerToggle.onClick { core.mediaService.toggle() }
        Event.playingChanged.listenUiThread {
            playerToggle.isSelected = !it.characteristic
        }

        fullScreenToggle.onClick { appView.toggleFullScreen() }
        fullScreenToggle.isSelected = appView.isFullScreen()

        initVolume()
        initTimes()

        nextColumn(flowPanel(hgap = 0, vhap = 0).apply {
            background = bgColor
            add(playerToggle)
        }) {
            width = 40
        }

        nextColumn(flowPanel(hgap = 0, vhap = 0) {
            background = bgColor
            addAll(volumeToggle, volumeSlider, volumeText)
        }) {
            width = 300
        }
        nextColumn(gridPanel {
            background = bgColor

            nextColumn(timeSlider) {
                fill = GridBagConstraints.HORIZONTAL
            }

            nextColumn(flowPanel(0, 0) {
                background = bgColor

                addAll(currentTimeText, totalTimeText)
            }) {
                width = 150
                fill = GridBagConstraints.NONE
            }

        }) {
            fill = GridBagConstraints.HORIZONTAL
        }
    }

    private fun initTimes() {
        Event.mediaDurationChange.listenUiThread {
            timeSlider.minimum = 0
            timeSlider.maximum = it.characteristic.toInt()

            totalTimeText.text = " / " + StringUtil.msToTimeLabel(it.characteristic)
        }

        Event.mediaTimeChange.listenUiThread {
            if (!timeSlider.valueIsAdjusting) {
                timeSlider.value = it.characteristic.toInt()
            }
            currentTimeText.text = StringUtil.msToTimeLabel(it.characteristic)
        }

        timeSlider.onChange { time, isAdjusting ->
            if (isAdjusting) {
                core.mediaService.setTime(time.toLong())
                currentTimeText.text = StringUtil.msToTimeLabel(time.toLong())
            }
        }
    }

    private fun initVolume() {
        volumeToggle.onClick {
            if (volumeToggle.isSelected) {
                lastVolumeSliderValue = volumeSlider.value
                volumeSlider.value = 0
            } else {
                volumeSlider.value = lastVolumeSliderValue
            }
        }

        volumeSlider.onChange { volume, isAdjusting ->
            volumeText.text = "$volume %"
            volumeToggle.isSelected = volume == 0

            if (!isAdjusting) core.mediaService.volume = volume
        }

        Event.volumeChanged.listenUiThread { volumeSlider.value = it }
    }

}