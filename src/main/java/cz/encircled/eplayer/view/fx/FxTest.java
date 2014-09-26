package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.core.container.Container;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Encircled on 18/09/2014.
 */
public class FxTest extends Application {

    public static final int MIN_WIDTH = 700;

    public static final int MIN_HEIGHT = 400;

    public Rectangle2D screenBounds;

    @Resource
    private QuickNaviScene quickNaviScene;

    @Resource
    private PlayerScene playerScene;

    @Resource
    private EventObserver eventObserver;

    private Stage primaryStage;

    private void showQuickNaviScene() {
        primaryStage.setScene(quickNaviScene);
        primaryStage.show();
    }

    public void showPlayerScene() {
        primaryStage.setScene(playerScene);
        primaryStage.show();
    }

    public void maximize() {
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        screenBounds = Screen.getPrimary().getVisualBounds();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        this.primaryStage = primaryStage;
        maximize();

        new Thread(() -> {
            Container c = new Container();
            c.addComponent(this);
            try {
                c.initializeContext();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
            eventObserver.fire(Event.contextInitialized);
        }).start();
    }

    @Resource
    private MediaService mediaService;

    @PostConstruct
    private void postInit() {
        Platform.runLater(() -> {
            quickNaviScene.getStylesheets().add("/stylesheet.css");
            playerScene.getStylesheets().add("/stylesheet.css");

            quickNaviScene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN), new Runnable() {
                @Override
                public void run() {

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Open Resource File");
                    String path = fileChooser.showOpenDialog(primaryStage).getPath();
                    showPlayerScene();
                    new Thread(() -> {
                        mediaService.play(path);
                    }).start();
                }
            });
            playerScene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), new Runnable() {
                @Override
                public void run() {
                    showQuickNaviScene();
                }
            });

            playerScene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), new Runnable() {
                @Override
                public void run() {
                    if (primaryStage.isFullScreen()) {
                        primaryStage.setFullScreen(false);
                    } else {
                        primaryStage.setFullScreen(true);
                    }
                }
            });

            primaryStage.setScene(quickNaviScene);
            primaryStage.show();
        });
    }

    public static void main(String[] args) {
        launch();
    }

}
