package cz.encircled.eplayer.view.swing;

import cz.encircled.eplayer.util.GuiUtil;
import cz.encircled.eplayer.util.Localizations;
import cz.encircled.eplayer.view.swing.componensts.EPlayerJButton;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;

public class Components {

    public final static Color MAIN_BLUE_COLOR = new Color(73, 117, 255);

    public final static Color MAIN_GRAY_COLOR = new Color(85, 85, 85);

    public final static Color MAIN_LIGHT_GRAY_COLOR = new Color(235, 235, 235);

    public final static Border QUICK_NAVI_BUTTON_BORDER =
            new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_BLUE_COLOR), new LineBorder(new Color(230, 230, 230)));

    public final static Border QUICK_NAVI_BUTTON_HOVER_BORDER =
            new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Components.MAIN_GRAY_COLOR), new LineBorder(new Color(230, 230, 230)));

    @Resource
    private Localizations localizations;

    @Resource
    private GuiUtil guiUtil;

    @NotNull
    public static EPlayerJButton getButton(String text, String actionCommand, int width, int height, ActionListener actionListener, Color colorOnHover) {
        EPlayerJButton b = new EPlayerJButton(text, colorOnHover);
        b.setUI(new BasicButtonUI());
        b.setActionCommand(actionCommand);
        b.setPreferredSize(new Dimension(width, height));
        b.setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        b.setBorderPainted(false);
        b.addActionListener(actionListener);
        b.addMouseListener(GuiUtil.HOVER_MOUSE_LISTENER);
        return b;
    }

    @NotNull
    public JMenuItem getMenuItem(@NotNull String textToLocalize, String actionCommand) {
        JMenuItem i = new JMenuItem(localizations.get(textToLocalize));
        i.setActionCommand(actionCommand);
        i.addActionListener(guiUtil.defaultActionListener);
        return i;
    }

}
