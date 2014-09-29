package cz.encircled.eplayer.view.fx.components;

import cz.encircled.eplayer.ioc.component.annotation.Factory;
import cz.encircled.eplayer.ioc.component.annotation.Scope;
import cz.encircled.eplayer.ioc.factory.FxFactory;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.view.fx.FxUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import javax.annotation.Resource;

/**
 * Created by Encircled on 19/09/2014.
 */
@Resource
@Factory(FxFactory.class)
//@Runner(FxRunner.class)
@Scope(Scope.PROTOTYPE)
public class QuickNaviButton extends StackPane {

    public static final int WIDTH = 410;

    public static final int HEIGHT = 350;

    @Resource
    private MediaService mediaService;

    private MediaType mediaType;

    private Label title;

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public QuickNaviButton initialize() {
        getStyleClass().add("qn_video");
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);

        title = new Label(mediaType.getName());
        title.setPrefWidth(WIDTH);

        title.getStyleClass().add("title");
        getChildren().add(title);
        StackPane.setAlignment(title, Pos.TOP_CENTER);

        setOnMouseClicked(event -> FxUtil.workInNormalThread(() -> mediaService.play(mediaType.getPath())));
        return this;
    }

}
