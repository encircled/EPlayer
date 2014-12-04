package cz.encircled.eplayer.view.fx;

import cz.encircled.eplayer.ioc.component.annotation.Runner;
import cz.encircled.eplayer.ioc.core.container.Container;
import cz.encircled.eplayer.ioc.runner.FxRunner;
import cz.encircled.eplayer.service.MediaService;
import cz.encircled.eplayer.service.action.ActionCommands;
import cz.encircled.eplayer.service.action.ActionExecutor;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.service.event.EventObserver;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.AppView;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;

/**
 * Created by Encircled on 18/09/2014.
 */
@Runner(FxRunner.class)
public class FxView extends Application implements AppView {

    private Logger log = LogManager.getLogger();

    public static final int MIN_WIDTH = 860;

    public static final int MIN_HEIGHT = 600;

    public Rectangle2D screenBounds;

    private FileChooser mediaFileChooser;

    @Resource
    private QuickNaviScreen quickNaviScreen;

    @Resource
    private PlayerScreen playerScreen;

    @Resource
    private EventObserver eventObserver;

    @Resource
    private MediaService mediaService;

    @Resource
    private ActionExecutor actionExecutor;

    private Stage primaryStage;

    private Scene primaryScene;

    private StringProperty screenChangeProperty;

    public static final String QUICK_NAVI_SCREEN = "quickNavi";

    public static final String PLAYER_SCREEN = "player";

    @Override
    public void showQuickNavi() {
        primaryScene.setRoot(quickNaviScreen);
        screenChangeProperty.setValue(QUICK_NAVI_SCREEN);
    }

    @Override
    public void showPlayer() {
        primaryScene.setRoot(playerScreen);
        screenChangeProperty.setValue(PLAYER_SCREEN);
    }

    public StringProperty screenChangeProperty() {
        return screenChangeProperty;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        primaryStage.setFullScreen(fullScreen);
    }

    public void toggleFullScreen() {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }

    public boolean isFullScreen() {
        return primaryStage.isFullScreen();
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
        screenChangeProperty = new SimpleStringProperty();
        this.primaryStage = primaryStage;
        initializePrimaryStage();
        maximize();
        setFullScreen(true);

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

    private void initializePrimaryStage() {
        primaryStage.setTitle(TITLE);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setFullScreenExitHint("");
    }

    @Override
    public void openMedia() {
        File file = mediaFileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            mediaService.play(file.getAbsolutePath());
            new Thread(() -> {
                mediaFileChooser.setInitialDirectory(file.getParentFile());
                Settings.set(Settings.FC_OPEN_LOCATION, file.getParentFile().getAbsolutePath());
                Settings.save();
            }).run();
        }
    }

    @PostConstruct
    private void initialize() {
        primaryScene = new Scene(quickNaviScreen);
        screenChangeProperty.set(QUICK_NAVI_SCREEN);
        primaryScene.getStylesheets().add("/stylesheet.css");
        primaryScene.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        primaryScene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    String filePath = file.getAbsolutePath();
                    log.debug("DnD {}", filePath);
                    quickNaviScreen.addTab(filePath);
                    new Thread(() -> {
                        Settings.addToList(Settings.FOLDERS_TO_SCAN, filePath);
                        Settings.save();
                    }).start();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });


        initializeMediaFileChoose();

        primaryStage.setOnCloseRequest(t -> {
            actionExecutor.execute(ActionCommands.EXIT);
        });

        //  TODO search
//        primaryScene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });

        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    private void initializeMediaFileChoose() {
        mediaFileChooser = new FileChooser();
        mediaFileChooser.setTitle(Localization.open.ln());
        String initialLocation = Settings.get(Settings.FC_OPEN_LOCATION);
        if (initialLocation != null) {
            File initialDirectory = new File(initialLocation);
            if (initialDirectory.exists())
                mediaFileChooser.setInitialDirectory(initialDirectory);
        }
    }

    public static void main(String[] args) throws Exception {
        launch();
    }

}
