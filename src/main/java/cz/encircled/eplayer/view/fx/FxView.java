package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.core.container.Container;
import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.view.AppView;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Encircled on 16/09/2014.
 */
public class FxView extends Application implements AppView {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private MediaService mediaService;

    @Override
    public void showPlayer() {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 400, 350);

        borderPane.setCenter(canvas);

        canvas.setWidth(400);
        canvas.setHeight(300);

        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> showQuickNavi(new ArrayList<>())
        );

        primaryStage.setScene(scene);
    }

    @Override
    public void addTabForFolder(@NotNull String tabName) {

    }

    @Override
    public void addTabForFolder(@NotNull String tabName, @NotNull Collection<MediaType> mediaType) {
        Tab newTab = new Tab(tabName);
        FlowPane flowPane = new FlowPane();
        mediaType.forEach(medit -> {
            flowPane.getChildren().add(new Button(medit.getName()));
        });
        newTab.setContent(flowPane);
    }

    @Override
    public void showQuickNavi(@NotNull Collection<MediaType> mediaType) {
        log.debug("Show quick navi, media count {}", mediaType.size());
        Scene scene = new Scene(new VBox(), 400, 350);
        primaryStage.setScene(scene);

        FlowPane flow = new FlowPane();
        flow.setVgap(8);
        flow.setHgap(4);
        mediaType.stream().forEach(media -> {
            Button button = new Button(media.getName());
            button.setMinWidth(50.0);
            button.setMinHeight(50.0);
            flow.getChildren().add(button);
        });
        naviTab.setContent(flow);
        tabPane.requestLayout();
    }

    @Override
    public void enterFullScreen() {

    }

    @Override
    public void exitFullScreen() {

    }

    @Override
    public void showShutdownTimeChooser() {

    }

    @Override
    public void enableSubtitlesMenu(boolean isEnabled) {

    }

    @Override
    public void showFilterInput() {

    }

    @Override
    public void hideFilterInput() {

    }

    @Override
    public void openMedia() {

    }

    private TabPane tabPane;

    private Tab naviTab;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("file.encoding", "UTF-8");

        this.primaryStage = primaryStage;

        Scene scene = new Scene(new VBox(), 400, 350);
        primaryStage.setScene(scene);

        MenuBar menuBar = new MenuBar();
        scene.getStylesheets().add("/stylesheet.css");

        Menu menuFile = new Menu("File");
        Menu menuEdit = new Menu("Edit");
        Menu menuView = new Menu("View");

        MenuItem open = new MenuItem("Open");
        open.addEventHandler(EventType.ROOT, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Resource File");
                mediaService.play(fileChooser.showOpenDialog(primaryStage).getPath());
            }
        });

        menuFile.getItems().add(open);
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar);

        tabPane = new TabPane();
        naviTab = new Tab("Navi");
        naviTab.setClosable(false);
        tabPane.getTabs().add(naviTab);

        ((VBox) scene.getRoot()).getChildren().add(tabPane);

        primaryStage.show();

        new Thread(() -> {
            Container c = new Container();
            c.addComponent(this);
            try {
                c.initializeContext();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }).start();
    }

    @Override
    public PixelWriter getPixelWriter() {

        return canvas.getGraphicsContext2D().getPixelWriter();
    }

    private Canvas canvas = new Canvas(400, 300);

    public static void main(String[] args) {
        launch();
    }

}
