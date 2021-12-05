package cz.encircled.eplayer.view.swing.components

import cz.encircled.eplayer.model.AppSettings
import cz.encircled.eplayer.service.event.Event
import cz.encircled.eplayer.util.StringUtil
import cz.encircled.eplayer.view.AppView
import cz.encircled.eplayer.view.controller.PlayerController
import cz.encircled.fswing.*
import cz.encircled.fswing.components.FluentPanel
import cz.encircled.fswing.components.FluentToggleButton
import cz.encircled.fswing.model.Colours.MEDIUM_BG
import cz.encircled.fswing.model.DualState
import cz.encircled.fswing.model.GridData
import java.awt.GridBagConstraints
import javax.swing.JLabel
import javax.swing.JSlider

class PlayerControls(appView: AppView, val settings: AppSettings, val controller: PlayerController) : FluentPanel() {

    private val playerToggle: FluentToggleButton = FluentToggleButton(iconName = DualState("pause.png", "play.png"))

    private val volumeToggle: FluentToggleButton =
        FluentToggleButton(iconName = DualState("volume.png", "volume_mute.png"))
    private val volumeSlider: JSlider = JSlider(0, settings.maxVolume, settings.lastVolume)
    private val volumeText: JLabel = JLabel("${settings.lastVolume} %")
    private var lastVolumeSliderValue = 0

    private val fullScreenToggle: FluentToggleButton = FluentToggleButton(iconName = DualState("fit.png", "shrink.png"))

    private val timeSlider: JSlider = JSlider()
    private val currentTimeText: JLabel = JLabel(StringUtil.msToTimeLabel(0L))
    private val totalTimeText: JLabel = JLabel(StringUtil.msToTimeLabel(0L))

    init {
        background = MEDIUM_BG
        preferredSize = AppView.MIN_WIDTH x AppView.PLAYER_CONTROLS_HEIGHT

        playerToggle.onClick { controller.togglePlaying() }
        Event.playingChanged.listenUiThread {
            playerToggle.isSelected = !it.characteristic
        }

        fullScreenToggle.onClick { appView.toggleFullScreen() }
        fullScreenToggle.isSelected = appView.isFullScreen()

        initVolume()
        initTimes()

        padding(20, 15)
        nextColumn(40, 40) {
            flowPanel(hgap = 0, vhap = 0) {

                background = MEDIUM_BG
                add(playerToggle)
            }
        }

        nextColumn(300, 40) {
            flowPanel(hgap = 0, vhap = 0) {
                background = MEDIUM_BG
                addAll(volumeToggle, volumeSlider, volumeText)
            }
        }
        nextColumn(GridData(fill = GridBagConstraints.HORIZONTAL)) {
            gridPanel {
                background = MEDIUM_BG

                nextColumn(GridData(fill = GridBagConstraints.HORIZONTAL)) {
                    timeSlider
                }

                nextColumn(GridData(150, fill = GridBagConstraints.NONE)) {
                    flowPanel(0, 0) {
                        background = MEDIUM_BG

                        addAll(currentTimeText, totalTimeText)
                    }
                }
            }
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
                controller.time = time.toLong()
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

            if (!isAdjusting) controller.volume = volume
        }

        Event.volumeChanged.listenUiThread { volumeSlider.value = it }
    }

}