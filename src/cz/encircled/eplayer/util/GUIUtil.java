package cz.encircled.eplayer.util;

import cz.encircled.eplayer.core.Application;
import cz.encircled.eplayer.view.listeners.BackgroundFocusListener;
import cz.encircled.eplayer.view.listeners.HoverMouseListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static cz.encircled.eplayer.util.LocalizedMessages.CONFIRM_TITLE;

/**
 * Created by Encircled on 7/06/2014.
 */
public class GUIUtil {

    private static JFrame frame;

    public static final MouseAdapter FILE_CHOOSER_MOUSE_ADAPTER = new MouseAdapter() {
        @Override
        public void mouseClicked(@NotNull MouseEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fc.showOpenDialog(SwingUtilities.getRoot(e.getComponent())) == JFileChooser.APPROVE_OPTION)
                ((JTextField)e.getSource()).setText(fc.getSelectedFile().getAbsolutePath());
        }
    };

    public static final ActionListener DEFAULT_ACTION_LISTENER = e -> Application.getActionExecutor().execute(e.getActionCommand());

    public static final HoverMouseListener HOVER_MOUSE_LISTENER = new HoverMouseListener();

    public static final BackgroundFocusListener BACKGROUND_FOCUS_LISTENER = new BackgroundFocusListener();

    public static void showMessage(String text, String title, int level){
        JOptionPane.showMessageDialog(frame, MessagesProvider.get(text),
                MessagesProvider.get(title), level);
    }

    public static boolean userConfirmed(String confirmMessage) {
        return JOptionPane.showConfirmDialog(frame, MessagesProvider.get(confirmMessage),
                                MessagesProvider.get(CONFIRM_TITLE), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void setFrame(JFrame frame){
        GUIUtil.frame = frame;
    }



}
