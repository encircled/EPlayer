package cz.encircled.eplayer.util;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.view.listeners.BackgroundFocusListener;
import cz.encircled.eplayer.view.listeners.HoverMouseListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Encircled on 7/06/2014.
 */
public class GUIConstants {

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

}
