package cz.encircled.eplayer.view.fx.components.qn;

import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import cz.encircled.eplayer.util.DateUtil;
import cz.encircled.eplayer.util.StringUtil;
import cz.encircled.eplayer.view.fx.FxUtil;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import cz.encircled.eplayer.view.fx.components.SimpleButton;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNull;

/**
 * @author Encircled on 19/09/2014.
 */
public class QuickNaviButton extends BorderPane {

    private MediaType mediaType;

    private SeriesVideo seriesVideo;
    private ApplicationCore core;
    private QuickNaviScreen screen;

    public QuickNaviButton(ApplicationCore core, QuickNaviScreen screen) {
        this.core = core;
        this.screen = screen;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setSeriesVideo(SeriesVideo seriesVideo) {
        this.seriesVideo = seriesVideo;
    }

    @NotNull
    public QuickNaviButton initialize() {
        getStyleClass().add("qn_video");

        setTop(initializeTitle());
        setBottom(initializeStatusBar());

        setOnMouseClicked(event -> FxUtil.workInNormalThread(() -> core.getMediaService().play(mediaType.getPath())));
        return this;
    }

    @NotNull
    private HBox initializeStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.getStyleClass().add("status_bar");

        if (mediaType.getTime() > 0L) {
            Label timeLabel = new Label(DateUtil.daysBetweenLocalized(mediaType.getWatchDate()) + ", " +
                    StringUtil.msToTimeLabel(mediaType.getTime()));
            statusBar.getChildren().add(timeLabel);
        }

        if (seriesVideo != null) {
            SimpleButton next = new SimpleButton("action_button", new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    core.getMediaService().play(seriesVideo.getNext());
                }
            }, "Next");
            statusBar.getChildren().add(next);
        }

        return statusBar;
    }

    @NotNull
    private BorderPane initializeTitle() {
        Label titleText = new Label(mediaType.getName());
        titleText.getStyleClass().add("text");

        SimpleButton removeButton = new SimpleButton("remove", event -> {
            FxUtil.workInNormalThread(() -> {
                core.getCacheService().deleteEntry(mediaType.getId());
                Platform.runLater(screen::refreshCurrentTab);
            });
            event.consume();
        });

        BorderPane title = new BorderPane();
        title.setLeft(titleText);
        title.setRight(removeButton);
        title.getStyleClass().add("title");
        return title;
    }

}
