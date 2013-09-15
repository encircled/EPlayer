package cz.encircled.eplayer.util;

import cz.encircled.eplayer.app.Application;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Encircled
 * Date: 9/14/13
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class GUIUtil {

    public static void bindKey(JComponent c, String key, Character charKey, final String command){
        if(key == null)
            c.getInputMap().put(KeyStroke.getKeyStroke(charKey), command);
        else
            c.getInputMap().put(KeyStroke.getKeyStroke(key), command);
        c.getActionMap().put(command, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Application.getInstance().getActionExecutor().execute(command);
            }
        });
    }

}
