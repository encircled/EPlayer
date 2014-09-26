package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.ioc.core.container.Context;
import cz.encircled.eplayer.model.MediaType;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Encircled on 20/09/2014.
 */
@Resource
public abstract class MediaTab extends Tab {

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
        Task<Collection<QuickNaviButton>> cacheTask = new Task<Collection<QuickNaviButton>>() {
            @Override
            protected Collection<QuickNaviButton> call() throws Exception {
                Collection<MediaType> mediaTypes = getMediaTypes();
                Collection<QuickNaviButton> buttons = new ArrayList<>(mediaTypes.size());
                mediaTypes.forEach(media -> {
                    QuickNaviButton button = context.getComponent(QuickNaviButton.class);
                    button.setMediaType(media);
                    buttons.add(button);
                });
                return buttons;
            }
        };
        cacheTask.setOnSucceeded(event -> {
            Collection<QuickNaviButton> buttons = cacheTask.getValue();
            buttons.forEach(QuickNaviButton::initialize);
            mainPane.getChildren().setAll(buttons);
        });
        cacheTask.run();
    }

    protected abstract Collection<MediaType> getMediaTypes();

}
