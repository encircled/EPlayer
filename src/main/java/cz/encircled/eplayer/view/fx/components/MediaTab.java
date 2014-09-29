package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.model.MediaType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
@Scope(Scope.PROTOTYPE)
public abstract class MediaTab extends Tab {

    private final static Logger log = LogManager.getLogger();

    protected FlowPane mainPane;

    @Resource
    private Context context;

    public MediaTab() {
        mainPane = new FlowPane(10, 10);
        ScrollPane scrollPane = new ScrollPane(mainPane);

        scrollPane.getStyleClass().add("tabs");
        mainPane.getStyleClass().add("tabs");
        getStyleClass().add("tabs");

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPadding(new Insets(10, 10, 10, 10));

        setContent(scrollPane);
        mainPane.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(20));
    }

    public void onShow() {
        // TODO cache if needed
        new Thread(() -> {
            Collection<MediaType> mediaTypes = getMediaTypes();
            final Collection<QuickNaviButton> buttons = new ArrayList<>(mediaTypes.size());
            mediaTypes.forEach(media -> {
                QuickNaviButton button = context.getComponent(QuickNaviButton.class);
                button.setMediaType(media);
                buttons.add(button);
            });
            Platform.runLater(() -> {
                log.debug("Media tab on show, add {} buttons", buttons.size());
                buttons.forEach(QuickNaviButton::initialize);
                mainPane.getChildren().setAll(buttons);
            });
        }).start();
    }

    protected abstract Collection<MediaType> getMediaTypes();

}
