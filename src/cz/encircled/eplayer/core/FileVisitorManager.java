package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cz.encircled.eplayer.common.Constants.DOT;
import static cz.encircled.eplayer.common.Constants.ONE;
import static cz.encircled.eplayer.common.Constants.ZERO;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Encircled on 7/06/2014.
 */
public class FileVisitorManager {

    private static final Logger log = LogManager.getLogger();

    private Map<Path, Map<Integer, MediaType>> paths;

    private static WatchService watcher;

    private static final String[] SUPPORTED_FORMATS = new String[]{"avi", "mkv", "mp3", "wav", "wmv"};

    private static final PlayableFileVisitor visitor = new PlayableFileVisitor();

    public FileVisitorManager(){
        paths = new HashMap<>();
        paths.put(Paths.get("C:\\Program Files\\"), new HashMap<>());
        initialize();
    }

    public Map<Path, Map<Integer, MediaType>> getPaths() {
        return paths;
    }

    private void initialize() {
        new Thread(() ->{
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
        }).start();
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

        private Map<Integer, MediaType> playable;

        public void setCurrentPlayable(Map<Integer, MediaType> playable){
            this.playable = playable;
        }

        @NotNull
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
            String name = file.toAbsolutePath().toString();
            if(Arrays.binarySearch(SUPPORTED_FORMATS, name.toLowerCase().substring(name.lastIndexOf(DOT) + ONE)) >= ZERO){
                playable.putIfAbsent(name.hashCode(), new MediaType(name));
                log.debug("Supported File {}", name);
            }

            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

    }

}
