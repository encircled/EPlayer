package cz.encircled.eplayer.view.fx.components.qn;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
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

import javax.annotation.Resource;

/**
 * Created by Encircled on 19/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Scope(Scope.PROTOTYPE)
public class QuickNaviButton extends BorderPane {

    @Resource
    private MediaService mediaService;

    @Resource
    private QuickNaviScreen quickNaviScreen;

    @Resource
    private CacheService cacheService;

    private MediaType mediaType;

    private SeriesVideo seriesVideo;

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public void setSeriesVideo(SeriesVideo seriesVideo) {
        this.seriesVideo = seriesVideo;
    }

    public QuickNaviButton initialize() {
        getStyleClass().add("qn_video");

        setTop(initializeTitle());
        setBottom(initializeStatusBar());

        setOnMouseClicked(event -> FxUtil.workInNormalThread(() -> mediaService.play(mediaType.getPath())));
        return this;
    }

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
                    mediaService.play(seriesVideo.getNext(
                    ));
                }
            }, "Next");
            statusBar.getChildren().add(next);
        }

        return statusBar;
    }

    private BorderPane initializeTitle() {
        Label titleText = new Label(mediaType.getName());
        titleText.getStyleClass().add("text");

        SimpleButton removeButton = new SimpleButton("remove", event -> {
            FxUtil.workInNormalThread(() -> {
                cacheService.deleteEntry(mediaType.getId());
                Platform.runLater(quickNaviScreen::refreshCurrentTab);
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
