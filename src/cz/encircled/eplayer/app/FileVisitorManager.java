package cz.encircled.eplayer.app;

import cz.encircled.eplayer.model.Playable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static cz.encircled.eplayer.common.Constants.DOT;
import static cz.encircled.eplayer.common.Constants.ONE;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Encircled on 7/06/2014.
 */
public class FileVisitorManager {

    private static final Logger log = LogManager.getLogger();

    private Map<Path, Map<Integer, Playable>> paths;

    private static WatchService watcher;

    private static final String SUPPORTED_FORMATS = "avi mkv mp3 wav wmv";

    private static final PlayableFileVisitor visitor = new PlayableFileVisitor();

    public FileVisitorManager(){
        paths = new HashMap<>();
        paths.put(Paths.get("D:\\video"), new HashMap<>());
        initialize();
    }

    public Map<Path, Map<Integer, Playable>> getPaths() {
        return paths;
    }

    private void initialize() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e){
            log.error("DirectoryScanner Initialize exception", e);
        }
        paths.forEach((path, playable) -> {
            try {
                path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } catch (IOException e) {
                log.error("Failed to register watcher on {}", path.toString());
            }
        });
        scanDirectories();
        startWatcher();
    }

    private void startWatcher() {
        new Thread(() -> {
            for (;;) {
                try {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> e : key.pollEvents()) {
                        scanDirectories();
                        if (!key.reset())
                            break;
                    }
                    Thread.sleep(1000);
                } catch(Exception e){
                    log.error("Watcher error: ", e);
                }
            }
        }).start();
    }

    private void scanDirectories(){
        paths.forEach((directoryPath, playable) -> {
            visitor.setCurrentPlayable(playable);
            playable.clear();
            try {
                Files.walkFileTree(directoryPath, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static class PlayableFileVisitor implements FileVisitor<Path> {

        private Map<Integer, Playable> playable;

        public void setCurrentPlayable(Map<Integer, Playable> playable){
            this.playable = playable;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String name = file.toAbsolutePath().toString();
            if(SUPPORTED_FORMATS.contains(name.toLowerCase().substring(name.lastIndexOf(DOT) + ONE))){
                playable.putIfAbsent(name.hashCode(), new Playable(name));
                log.debug("Supported File {}", name);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

    }

}
