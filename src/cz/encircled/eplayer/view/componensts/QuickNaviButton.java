package cz.encircled.eplayer.view.componensts;

import cz.encircled.eplayer.app.Application;
import cz.encircled.eplayer.model.Playable;
import cz.encircled.eplayer.util.GUIConstants;
import cz.encircled.eplayer.view.Components;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class QuickNaviButton extends JButton {

    public static final Color BACKGROUND = new Color(245, 245, 245);

	private static final Dimension DIMENSION =  new Dimension(350, 190);

    private static ActionListener CLICK_LISTENER;

    private final Playable playable;

    private final Application app;

    private boolean canBeDeleted = true;

	public QuickNaviButton(Application app, Playable p){
        this.app = app;
        CLICK_LISTENER = e -> app.play(((QuickNaviButton)e.getSource()).getPlayable());
		playable = p;
		initialize();
	}

    public QuickNaviButton(Application app, Playable p, boolean canBeDeleted){
        this.app = app;
        this.canBeDeleted = canBeDeleted;
        CLICK_LISTENER = e -> app.play(((QuickNaviButton)e.getSource()).getPlayable().getPath());
        playable = p;
        initialize();
    }

	private void initialize(){
		setText(buildHTML());
        setUI(new BasicButtonUI());
        setPreferredSize(DIMENSION);
        setBackground(Components.MAIN_LIGHT_GRAY_COLOR);
        setBorder(Components.QUICK_NAVI_BUTTON_BORDER);
        setBackground(BACKGROUND);
        addActionListener(CLICK_LISTENER); // TODO move
        addMouseListener(GUIConstants.HOVER_MOUSE_LISTENER);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        setToolTipText("At " + playable.getPath());

        if(canBeDeleted) {
            ActionListener deleteListener = e -> app.deletePlayable(playable.getPath().hashCode());
            JButton deleteButton = Components.getButton("x", "", 25, 25, deleteListener, new Color(255, 81, 81));
            deleteButton.setBackground(new Color(253, 253, 253));
            deleteButton.setBorder(new LineBorder(new Color(235, 235, 235)));
            add(deleteButton);
        }
	}
	
	@NotNull
    private String buildHTML(){
        long ms = playable.getTime();
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));

		StringBuilder builder = new StringBuilder("<html><p style=\"white-space: pre-line;word-brake:normal;width:250px;\"><font size=\"6\">");
		builder.append(playable.getName()).append("</font></p><br/><font color=\"rgb(85,85,85)\">Current time: ").append(String.format("%02d:%02d:%02d", h,m,s)).append("</font>");
		if(!playable.exists())
			builder.append("</font><br/><br/><font size=\"3\" color=\"rgb(85,85,85)\">no longer exists at ").append(playable.getPath()).append("</font>");
		return builder.append("</html>").toString();
	}
	
	public Playable getPlayable(){
		return playable;
	}
	
}
