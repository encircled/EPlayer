package cz.encircled.eplayer.view.componensts;

import cz.encircled.eplayer.common.Constants;
import cz.encircled.eplayer.util.PropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/13/13
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlayerControls extends JPanel {

    private final static Logger log = LogManager.getLogger(PlayerControls.class);

    private static final int DEFAULT_MAX_VOLUME = 150;

    private static final String LENGTH_LABEL_PREFIX = "/  ";

    private final EmbeddedMediaPlayer player;

    private JSlider positionSlider;

    private JSlider volumeSlider;

    private JLabel volumeLabel;

    private JRadioButton volumeButton;

    private JLabel timeLabel;

    private JLabel lengthLabel;

    private JLabel positionLabel;

    private static final String TIME_SEPARATOR = ":";

    public PlayerControls(@NotNull EmbeddedMediaPlayer player){
        this.player = player;
        initialize();
    }

    public void reinitialize(){
        log.debug("Reinitializing controls panel. New length is {}", player.getLength());
        lengthLabel.setText(LENGTH_LABEL_PREFIX + buildTimeTextLabel((int) player.getLength()));
        positionSlider.setMaximum((int) player.getLength());
        positionSlider.setEnabled(true);
        if(player.getTime() > Constants.ZERO)
            positionSlider.setValue((int) player.getTime());
    }

    private static void setSliderToCursorPosition(MouseEvent e, JSlider slider){
        double percent = e.getPoint().x / ((double) slider.getWidth());
        int range = slider.getMaximum() - slider.getMinimum();
        double newVal = range * percent;
        slider.setValue((int)(slider.getMinimum() + newVal));
    }

    private static String buildTimeTextLabel(int ms){
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));

        StringBuilder sb = new StringBuilder();
        appendZeroIfMissing(sb, h);
        sb.append(h).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, m);
        sb.append(m).append(TIME_SEPARATOR);
        appendZeroIfMissing(sb, s);
        sb.append(s);

        return sb.toString();
    }

    private static void appendZeroIfMissing(StringBuilder sb, long digit){
        if(digit < Constants.TEN)
            sb.append(Constants.ZERO_STRING);
    }

    private void initialize(){
        setPreferredSize(new Dimension(1700, 53));
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        JPanel bottom = new JPanel();
        top.setPreferredSize(new Dimension((int) getSize().getWidth(), 18));
        bottom.setPreferredSize(new Dimension((int) getSize().getWidth(), 35));
        top.setLayout(null);
        bottom.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        initializePositionSlider();
        initializeVolumeSlider();
        initializeLabels();
        initializeVolumeButton();


        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.CENTER);

        top.add(positionLabel);
        bottom.add(volumeButton);
        bottom.add(volumeSlider);
        bottom.add(volumeLabel);
        bottom.add(positionSlider);
        bottom.add(timeLabel);
        bottom.add(lengthLabel);
    }


    private void initializeVolumeButton() {
        volumeButton = new JRadioButton();
        volumeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/volume_small.png")));
        volumeButton.setSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/volume-mute_small.png")));
        volumeButton.setRolloverSelectedIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/volume-mute_small.png")));
        volumeButton.addActionListener(new ActionListener() {

            private int lastVolume;

            @Override
            public void actionPerformed(ActionEvent e) {
                if(volumeButton.isSelected()){
                    lastVolume = player.getVolume();
                    volumeSlider.setValue(0);
                }  else
                    volumeSlider.setValue(lastVolume);
            }
        });
    }

    private void initializeLabels() {
        timeLabel = new JLabel();
        lengthLabel = new JLabel();
        volumeLabel = new JLabel(volumeSlider.getValue() + " %");
        volumeLabel.setPreferredSize(new Dimension(40, 20));
        positionLabel = new JLabel();
        positionLabel.setVisible(false);
    }

    private void initializeVolumeSlider() {
        volumeSlider = new JSlider();
        volumeSlider.setMaximum(PropertyProvider.getInt(PropertyProvider.SETTING_MAX_VOLUME, DEFAULT_MAX_VOLUME));
        volumeSlider.setMinimum(Constants.ZERO);
        volumeSlider.setPreferredSize(new Dimension(150, 20));
        volumeSlider.setSize(new Dimension(150, 20));
        volumeSlider.setValue(player.getVolume());
        volumeSlider.addChangeListener(e -> {
            player.setVolume(volumeSlider.getValue());
            volumeLabel.setText(volumeSlider.getValue() + " %");
            if(volumeSlider.getValue() == 0){
                volumeButton.setSelected(true);
            } else {
                if(volumeButton.isSelected())
                    volumeButton.setSelected(false);
            }
        });
        volumeSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSliderToCursorPosition(e, volumeSlider);
            }
        });
    }

    private void initializePositionSlider() {
        positionSlider = new JSlider();
        positionSlider.setEnabled(false);
        positionSlider.setFocusable(false);
        positionSlider.setPreferredSize(new Dimension(800, 20));
        positionSlider.setMinimum(Constants.ZERO);
        positionSlider.addChangeListener(e -> SwingUtilities.invokeLater(() -> {
            if(getMousePosition() != null) {
                positionLabel.setBounds((int) getMousePosition().getX(), 3, 100, 15);
                positionLabel.setText(buildTimeTextLabel(positionSlider.getValue()));
            }
        }));
        positionSlider.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                positionLabel.setVisible(false);
                player.setTime(positionSlider.getValue());
                player.start();
                log.debug("Slider released, value is {}", positionSlider.getValue());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                positionLabel.setVisible(true);
                player.pause();
                setSliderToCursorPosition(e, positionSlider);
            }

        });
    }

    public void fireTimeChanged(Long newTime){
        if(!positionSlider.getValueIsAdjusting())
            positionSlider.setValue(newTime.intValue());
        timeLabel.setText(buildTimeTextLabel(newTime.intValue()));
    }

}
