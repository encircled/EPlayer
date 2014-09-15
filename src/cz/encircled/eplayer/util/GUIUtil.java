package cz.encircled.eplayer.util;

import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.view.swing.listeners.HoverMouseListener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import javax.swing.*;
import java.awt.event.ActionListener;

import static cz.encircled.eplayer.util.LocalizedMessages.CONFIRM_TITLE;

/**
 * Created by Encircled on 7/06/2014.
 */
public class GuiUtil {

    private static JFrame frame;

    @Resource
    private Localizations localizations;

    @Resource
    private ActionExecutor actionExecutor;

    public ActionListener defaultActionListener;

    public static final HoverMouseListener HOVER_MOUSE_LISTENER = new HoverMouseListener();

    public GuiUtil() {
        defaultActionListener = e -> actionExecutor.execute(e.getActionCommand());
    }

    public void showMessage(@NotNull String text, @NotNull String title, int level) {
        JOptionPane.showMessageDialog(frame, localizations.get(text),
                localizations.get(title), level);
    }

    public boolean userConfirmed(@NotNull String confirmMessage) {
        return JOptionPane.showConfirmDialog(frame, localizations.get(confirmMessage),
                localizations.get(CONFIRM_TITLE), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void setFrame(JFrame frame) {
        GuiUtil.frame = frame;
    }


}
