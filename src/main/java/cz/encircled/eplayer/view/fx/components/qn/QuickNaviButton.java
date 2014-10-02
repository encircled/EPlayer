package cz.encircled.eplayer.view.fx.components.qn;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.model.SeriesVideo;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.view.fx.FxUtil;
import cz.encircled.eplayer.view.fx.QuickNaviScreen;
import cz.encircled.eplayer.view.fx.components.ImageButton;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import javax.annotation.Resource;

/**
 * Created by Encircled on 19/09/2014.
 */
@Resource
@Factory(FxFactory.class)
@Scope(Scope.PROTOTYPE)
public class QuickNaviButton extends BorderPane {

    public static final int WIDTH = 410;

    public static final int HEIGHT = 350;

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
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);

        setTop(initializeTitle());

        setOnMouseClicked(event -> FxUtil.workInNormalThread(() -> mediaService.play(mediaType.getPath())));

        if(seriesVideo != null) {
            Button next = new Button("Next");
            next.setOnAction(event -> {
                mediaService.play(seriesVideo.getNext());
            });
            setBottom(next);
        }

        return this;
    }

    private BorderPane initializeTitle() {
        Label titleText = new Label(mediaType.getName());
        titleText.setMaxWidth(WIDTH - 50);
        titleText.getStyleClass().add("text");

        ImageButton removeButton = new ImageButton("remove", event -> {
            FxUtil.workInNormalThread(() -> {
                cacheService.deleteEntry(mediaType.hashCode());
                Platform.runLater(quickNaviScreen::refreshCurrentTab);
            });
        });

        BorderPane title = new BorderPane();
        title.setLeft(titleText);
        title.setRight(removeButton);
        title.getStyleClass().add("title");
        return title;
    }

}
