package cz.encircled.eplayer.view.componensts;

import cz.encircled.eplayer.app.PropertyProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
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

    private EmbeddedMediaPlayer player;

    private JSlider positionSlider;

    private JSlider volumeSlider;

    private JLabel volumeLabel;

    private JRadioButton volumeButton;

    private JLabel timeLabel;

    private JLabel lengthLabel;

    private JLabel positionLabel;

    public PlayerControls(EmbeddedMediaPlayer player){
        this.player = player;
        initialize();
    }

    public void reinitialize(){
        Object[] time = parseTime(player.getLength());
        lengthLabel.setText(String.format("/ %02d:%02d:%02d", time[0], time[1], time[2]));

        positionSlider.setPreferredSize(new Dimension(1200, 20));
        positionSlider.setFocusable(false);
        positionSlider.setMinimum(0);
        positionSlider.setMaximum((int) player.getLength() / 1000);
        positionSlider.setValue(0);

        positionSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
//                        positionLabel.setBounds((int)getMousePosition().getX(),5,70,15);
                    }
                });
//                Object[] time = parseTime(positionSlider.getValue() * 1000L);
//                positionLabel.setText(String.format("%02d:%02d:%02d", time[0], time[1], time[2]));

            }
        });
        positionSlider.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                player.setTime(positionSlider.getValue() * 1000);
                player.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                player.pause();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
        positionSlider.setEnabled(true);
        volumeSlider.setValue(player.getVolume());
        volumeLabel.setText(volumeSlider.getValue() + " %");
    }

    private void initialize(){
        setPreferredSize(new Dimension(200, 50));
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 18));

        positionSlider = new JSlider();
        positionSlider.setEnabled(false);

        intializeVolumeSlider();
        initializeLabels();
        initializeVolumeButton();
        positionLabel = new JLabel();
        add(volumeButton);
        add(volumeSlider);
        add(volumeLabel);
        add(positionSlider);
        add(positionLabel);
        add(timeLabel);
        add(lengthLabel);

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
        volumeLabel = new JLabel();
        volumeLabel.setPreferredSize(new Dimension(40, 20));
    }

    private void intializeVolumeSlider() {
        volumeSlider = new JSlider();
        volumeSlider.setEnabled(true);
        volumeSlider.setMaximum(PropertyProvider.getInt(PropertyProvider.SETTING_MAX_VOLUME, DEFAULT_MAX_VOLUME));
        volumeSlider.setMinimum(0);
        volumeSlider.setPreferredSize(new Dimension(150, 20));
        volumeSlider.setSize(new Dimension(150, 20));
        volumeSlider.setFocusable(false);
        volumeSlider.addChangeListener(new ChangeListener() {

            long lastC = 0L;
            @Override
            public void stateChanged(ChangeEvent e) {
                Long n = System.currentTimeMillis();
                if(n - lastC > 100L){
                    player.setVolume(volumeSlider.getValue());
                    volumeLabel.setText(volumeSlider.getValue() + " %");
                    lastC = n;
                    if(volumeSlider.getValue() == 0){
                        volumeButton.setSelected(true);
                    } else {
                        if(volumeButton.isSelected())
                            volumeButton.setSelected(false);
                    }
                }
            }
        });
    }

    public void fireTimeChanged(Long newTime){
        positionSlider.setValue((int) (newTime / 1000));
        Object[] t = parseTime(newTime);
        timeLabel.setText(String.format("%02d:%02d:%02d", t[0],t[1],t[2]));
    }

    private Object[] parseTime(Long time){
        long s = (time / 1000) % 60;
        long m = (time / (1000*60)) % 60;
        long h = (time / (1000*60*60)) % 24;
        return new Object[]{ h,m,s };
    }

    private class EPlayerJSlider extends JSlider {

        public EPlayerJSlider(){

        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

        }
    }

}
