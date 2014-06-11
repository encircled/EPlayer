package cz.encircled.eplayer.core;

import cz.encircled.eplayer.model.MediaType;
import cz.encircled.eplayer.service.CacheService;
import cz.encircled.eplayer.service.FileScanListener;
import cz.encircled.eplayer.service.FolderScanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static cz.encircled.eplayer.common.Constants.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by Encircled on 7/06/2014.
 */
public class FileVisitorScanService implements FolderScanService {

    private static final Logger log = LogManager.getLogger();

    private final CacheService cacheService;

    private Map<String, ScanFolder> foldersToScan;

    private static WatchService watcher;

    private static final String[] SUPPORTED_FORMATS = new String[]{"avi", "mkv", "mp3", "wav", "wmv"};

    private final PlayableFileVisitor visitor = new PlayableFileVisitor();

    private final List<FileScanListener> listeners;

    private boolean interrupted = false;

    public FileVisitorScanService(CacheService cacheService){
        this.cacheService = cacheService;
        foldersToScan = new HashMap<>();
        listeners = new ArrayList<>();
    }

    @Override
    public FolderScanService initialize() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e){
            log.error("DirectoryScanner Initialize exception", e);
            throw new RuntimeException("Can't start watcher service");
        }
        return this;
    }

    @Override
    public FolderScanService addFiledScanListener(FileScanListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public boolean addIfAbsent(String absolutePath) {
        if(!foldersToScan.containsKey(absolutePath)){
            try {
                foldersToScan.put(absolutePath, new ScanFolder(absolutePath).build());
                return true;
            } catch (IOException e) {
                log.error("Failed to register watcher on {}", absolutePath);
            }
        }
        return false;
    }

    @Override
    public FolderScanService addAllIfAbsent(String[] absolutePaths){
        for(String path : absolutePaths)
            addIfAbsent(path);
        return this;
    }

    @Override
    public boolean removeFolder(String absolutePath) {
        ScanFolder folder = foldersToScan.remove(absolutePath);
        if(folder != null){
            folder.key.cancel();
            return true;
        }
        return false;
    }

    @Override
    public void start(){
        scanDirectories();
        startWatcher();
    }

    @Override
    public void stop(){
        interrupted = true;
    }

    private void startWatcher() {
        new Thread(() -> {
            while (!interrupted) {
                try {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> e : key.pollEvents()) {
                        WatchEvent<Path> event = (WatchEvent<Path>) e;
                        String absolutePath = event.context().toAbsolutePath().toString();

                        getFoldersForPath(absolutePath).forEach((folder) -> {
                            if(event.kind() == ENTRY_DELETE) {
                                folder.media.remove(absolutePath.hashCode());
                            } else {
                                MediaType media = cacheService.getEntry(absolutePath.hashCode());
                                if(media == null)
                                    media = new MediaType(absolutePath);
                                folder.media.putIfAbsent(absolutePath.hashCode(), media);
                            }
                            fireFolderChanged(absolutePath, folder);
                        });

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

    private Collection<ScanFolder> getFoldersForPath(String path){
        Collection<ScanFolder> result = new ArrayList<>();
        foldersToScan.values().forEach((folder) -> {
            if(folder.absolutePath.contains(path))
                result.add(folder);
        });
        return result;
    }

    private void scanDirectories(){
        foldersToScan.forEach((directoryPath, folder) -> {
            visitor.setCurrentFolder(folder);
            folder.media.clear();
            try {
                Files.walkFileTree(folder.path, visitor);
                fireFolderScanned(directoryPath, folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void fireFolderScanned(String path, ScanFolder folder){
        listeners.stream().parallel().forEach((l) -> l.onFolderScanned(path, folder.media));
    }

    private void fireFolderChanged(String path, ScanFolder folder){
        listeners.stream().parallel().forEach((l) -> l.onFolderChange(path, folder.media));
    }

    private class PlayableFileVisitor implements FileVisitor<Path> {

        private ScanFolder folder;

        public void setCurrentFolder(ScanFolder folder){
            this.folder = folder;
        }

        @NotNull
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @NotNull
        @Override
        public FileVisitResult visitFile(@NotNull Path file, BasicFileAttributes attrs) throws IOException {
            String absolutePath = file.toAbsolutePath().toString();
            if(Arrays.binarySearch(SUPPORTED_FORMATS, absolutePath.toLowerCase().substring(absolutePath.lastIndexOf(DOT) + ONE)) >= ZERO){
                MediaType media = cacheService.getEntry(absolutePath.hashCode());
                if(media == null)
                    media = new MediaType(absolutePath);
                folder.media.putIfAbsent(absolutePath.hashCode(), media);
                log.debug("Supported File {}", absolutePath);
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

    private class ScanFolder {

        String absolutePath;

        Path path;

        WatchKey key;

        Map<Integer, MediaType> media;

        ScanFolder(String absolutePath){
            this.absolutePath = absolutePath;
            media = new HashMap<>();
        }

        ScanFolder build() throws IOException {
            path = Paths.get(absolutePath);
            key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            return this;
        }

    }

}
