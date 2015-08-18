package cz.encircled.eplayer.view.fx;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import cz.encircled.eplayer.core.ApplicationCore;
import cz.encircled.eplayer.service.event.Event;
import cz.encircled.eplayer.util.Localization;
import cz.encircled.eplayer.util.Settings;
import cz.encircled.eplayer.view.AppView;
import cz.encircled.eplayer.view.fx.components.AppMenuBar;
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
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.io.File;

/**
 * @author Encircled on 18/09/2014.
 */
public class FxView extends Application implements AppView {

    // TODO
//    public static final String VLC_LIB_PATH = "D:\\Soft\\vlc-2.2.1";
    public static final String VLC_LIB_PATH = "E:\\soft\\vlc-2.1.4";
    public static final int MIN_WIDTH = 860;
    public static final int MIN_HEIGHT = 600;
    public static final String QUICK_NAVI_SCREEN = "quickNavi";
    public static final String PLAYER_SCREEN = "player";
    private static final Logger log = LogManager.getLogger();

    static {
        log.trace("Initialize VLC libs");
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), VLC_LIB_PATH);
        try {
            Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
            log.trace("VLCLib successfully initialized");
        } catch (UnsatisfiedLinkError e) {
            // TODO
            log.error("Failed to load vlc libs from specified path {}", VLC_LIB_PATH);
        }
    }

    public Rectangle2D screenBounds;
    private FileChooser mediaFileChooser;
    private QuickNaviScreen quickNaviScreen;
    private PlayerScreen playerScreen;
    private Stage primaryStage;
    private Scene primaryScene;
    private StringProperty screenChangeProperty;
    private ApplicationCore core;

    public static void main(String[] args) throws Exception {
        launch();
    }

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

    public void toggleFullScreen() {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
    }

    public boolean isFullScreen() {
        return primaryStage.isFullScreen();
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        primaryStage.setFullScreen(fullScreen);
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
        this.screenBounds = Screen.getPrimary().getVisualBounds();
        this.screenChangeProperty = new SimpleStringProperty();
        this.primaryStage = primaryStage;

        core = new ApplicationCore();

        playerScreen = new PlayerScreen(core, this);
        quickNaviScreen = new QuickNaviScreen(core, this);
        primaryScene = new Scene(quickNaviScreen);

        screenChangeProperty.set(QUICK_NAVI_SCREEN);

        AppMenuBar menuBar = new AppMenuBar(core, this);
        quickNaviScreen.init(menuBar);
        menuBar.init();

        initializePrimaryStage();
        maximize();
        initialize();

        playerScreen.init(core, menuBar);

        core.initFx(playerScreen.getMediaPlayerComponent().getMediaPlayer());
        new Thread(() -> {
            core.init(this);
            core.getEventObserver().fire(Event.contextInitialized);
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
            core.getMediaService().play(file.getAbsolutePath());
            new Thread(() -> {
                mediaFileChooser.setInitialDirectory(file.getParentFile());
                Settings.fc_open_location.set(file.getParentFile().getAbsolutePath()).save();
            }).run();
        }
    }

    private void initialize() {
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
                        Settings.folders_to_scan.addToList(filePath).save();
                    }).start();
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        initializeMediaFileChoose();

        primaryStage.setOnCloseRequest(t -> core.exit());
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    private void initializeMediaFileChoose() {
        mediaFileChooser = new FileChooser();
        mediaFileChooser.setTitle(Localization.open.ln());
        String initialLocation = Settings.fc_open_location.get();
        if (initialLocation != null) {
            File initialDirectory = new File(initialLocation);
            if (initialDirectory.exists())
                mediaFileChooser.setInitialDirectory(initialDirectory);
        }
    }

    public PlayerScreen getPlayerScreen() {
        return playerScreen;
    }

    public QuickNaviScreen getQuickNaviScreen() {
        return quickNaviScreen;
    }

}
