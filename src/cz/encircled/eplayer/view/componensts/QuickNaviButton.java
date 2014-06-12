package cz.encircled.eplayer.view.componensts;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.ViewService;
import cz.encircled.eplayer.util.GUIUtil;
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

	private static final Dimension DIMENSION =  new Dimension(360, 190);

    private static ActionListener CLICK_LISTENER;

    private final MediaType mediaType;

    private boolean canBeDeleted = true;

    private final ViewService viewService;

    public QuickNaviButton(ViewService viewService, @NotNull MediaService mediaService, MediaType p){
        this.viewService = viewService;
        CLICK_LISTENER = e -> mediaService.play(((QuickNaviButton)e.getSource()).getMediaType());
		mediaType = p;
		initialize();
	}

    public QuickNaviButton(ViewService viewService, @NotNull MediaService mediaService, MediaType p, boolean canBeDeleted){
        this.viewService = viewService;
        this.canBeDeleted = canBeDeleted;
        CLICK_LISTENER = e -> mediaService.play(((QuickNaviButton)e.getSource()).getMediaType().getPath());
        mediaType = p;
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
        addMouseListener(GUIUtil.HOVER_MOUSE_LISTENER);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        setToolTipText("At " + mediaType.getPath());

        if(canBeDeleted) {
            ActionListener deleteListener = e -> {
                getParent().remove(this);
                viewService.deleteMedia(mediaType.getHash());
            };
            JButton deleteButton = Components.getButton("x", "", 25, 25, deleteListener, new Color(255, 81, 81));
            deleteButton.setBackground(new Color(253, 253, 253));
            deleteButton.setBorder(new LineBorder(new Color(235, 235, 235)));
            add(deleteButton);
        }
	}
	
	@NotNull
    private String buildHTML(){
        long ms = mediaType.getTime();
        long h = TimeUnit.MILLISECONDS.toHours(ms);
        long m = TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms));
        long s = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms));

		StringBuilder builder = new StringBuilder("<html><p style=\"white-space: pre-line;word-brake:normal;width:250px;\"><font size=\"6\">");
		builder.append(mediaType.getName()).append("</font></p><br/><font color=\"rgb(85,85,85)\">Current time: ").append(String.format("%02d:%02d:%02d", h,m,s)).append("</font>");
		if(!mediaType.exists())
			builder.append("</font><br/><br/><font size=\"3\" color=\"rgb(85,85,85)\">no longer exists at ").append(mediaType.getPath()).append("</font>");
		return builder.append("</html>").toString();
	}
	
	public MediaType getMediaType(){
		return mediaType;
	}
	
}
