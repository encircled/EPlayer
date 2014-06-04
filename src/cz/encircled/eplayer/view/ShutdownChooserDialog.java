package cz.encircled.eplayer.view;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.util.GUIUtil;
import cz.encircled.eplayer.util.KeyConstants;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.actions.ActionCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 10/12/13
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
class ShutdownChooserDialog extends JDialog {

    private static final Logger log = LogManager.getLogger(ShutdownChooserDialog.class);

    private static final Dimension DIALOG_DIMENSION = new Dimension(280, 110);

    private static final Dimension FIELD_DIMENSION = new Dimension(230, 30);

    private static final String NON_DIGITS_PATTERN = "\\D";

    private Long time;

    private String shutdownParam;

    private JCheckBox isHibernateBox;

    private JCheckBox isShutdownBox;

    private JTextField timeField;

    public ShutdownChooserDialog(JFrame parent){
        super(parent, true);
        initialize();
        setVisible(true);
    }

    private void initialize(){
        setPreferredSize(DIALOG_DIMENSION);
        setSize(DIALOG_DIMENSION);
        setLayout(new BorderLayout());
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        timeField = new JTextField();
        timeField.addActionListener(e -> {
            timeField.setText(timeField.getText().replaceAll(NON_DIGITS_PATTERN,""));
            String val = timeField.getText();
            if(StringUtil.isSet(val)){
                setTime(Long.parseLong(val));
                shutdownParam = isShutdownBox.isSelected() ? Application.SD_CMD_SHUTDOWN : Application.SD_CMD_HIBERNATE;
                dispose();
            }
        });
        isHibernateBox = new JCheckBox("hib");
        isShutdownBox= new JCheckBox("shut");

        wrapper.setPreferredSize(DIALOG_DIMENSION);
        wrapper.setBackground(Color.WHITE);

        timeField.setPreferredSize(FIELD_DIMENSION);

        add(wrapper, BorderLayout.CENTER);
        wrapper.add(timeField);
        wrapper.add(isHibernateBox);
        wrapper.add(isShutdownBox);
        setLocationRelativeTo(null);

        GUIUtil.bindKey(wrapper, KeyConstants.ESCAPE, null, ActionCommands.CANCEL);
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getShutdownParam() {
        return shutdownParam;
    }

    public void setShutdownParam(String shutdownParam) {
        this.shutdownParam = shutdownParam;
    }
}
