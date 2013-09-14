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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

    private EmbeddedMediaPlayer player;

    private JSlider positionSlider;

    private JLabel timeLabel;

    private JLabel lengthLabel;

    public PlayerControls(EmbeddedMediaPlayer player){
        this.player = player;
        initialize();
    }

    public void reinitialize(){
        Object[] time = parseTime(player.getLength());
        lengthLabel.setText(String.format("/ %02d:%02d:%02d", time[0], time[1], time[2]));
        positionSlider.setPreferredSize(new Dimension(500, 20));
        positionSlider.setFocusable(false);
        positionSlider.setMinimum(0);
        positionSlider.setMaximum((int) player.getLength() / 1000);
        positionSlider.setValue(0);

        positionSlider.setToolTipText("01:01");
        positionSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
//                System.out.println(((JSlider)e.getSource()).getValue());
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
    }

    private final void initialize(){
        setPreferredSize(new Dimension(200, 80));
        positionSlider = new JSlider();
        positionSlider.setEnabled(false);

        timeLabel = new JLabel();
//        timeLabel.setPreferredSize(new Dimension(,50));

        lengthLabel = new JLabel();
//        lengthLabel.setPreferredSize(new Dimension(80,50));

        add(positionSlider);
        add(timeLabel);
        add(lengthLabel);
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

}
