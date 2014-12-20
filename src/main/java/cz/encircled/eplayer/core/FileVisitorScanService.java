package cz.encircled.eplayer.core;

/**
 * Created by Encircled on 7/06/2014.
 */

public class FileVisitorScanService {
}/* implements FolderScanService {

    private static final Logger log = LogManager.getLogger();

    @Resource
    protected CacheService cacheService;

    @Resource
    private EventObserver eventObserver;

    private Map<String, ScanFolder> foldersToScan;

    private static WatchService watcher;

    private static final String[] SUPPORTED_FORMATS = new String[]{"avi", "mkv", "mp3", "wav", "wmv"};

    private final PlayableFileVisitor visitor = new PlayableFileVisitor();

    private boolean interrupted = false;

    public FileVisitorScanService() {
        foldersToScan = new HashMap<>();
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            log.error("DirectoryScanner Initialize exception", e);
            throw new RuntimeException("Can't start watcher service");
        }
    }

    @Override
    public boolean addIfAbsent(String absolutePath) {
        if (!foldersToScan.containsKey(absolutePath)) {
            try {
                ScanFolder newFolder = new ScanFolder(absolutePath).annotationToDefinition();
                foldersToScan.put(absolutePath, newFolder);
                scanDirectory(absolutePath, newFolder);
                return true;
            } catch (IOException e) {
                log.error("Failed to register watcher on {}", absolutePath);
            }
        }
        log.debug("{} is already watching", absolutePath);
        return false;
    }

    @Override
    public FolderScanService addAllIfAbsent(List<String> absolutePaths) {
        absolutePaths.forEach(this::addIfAbsent);
        return this;
    }

    @Override
    public boolean removeFolder(String absolutePath) {
        ScanFolder folder = foldersToScan.remove(absolutePath);
        if (folder != null) {
            folder.key.cancel();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        scanAllDirectories();
        startWatcher();
    }

    @Override
    public void stop() {
        interrupted = true;
    }

    private void startWatcher() {
        new Thread(() -> {
            while (!interrupted) {
                try {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> e : key.pollEvents()) {
                        WatchEvent<Path> event = (WatchEvent<Path>) e;

                        Path folderPath = (Path) key.watchable();
                        String absolutePath = folderPath + "\\" + event.context().toString();

                        log.debug("File {} has changed, kind is {}", absolutePath, event.kind());
                        ScanFolder folder = getScanFolder(folderPath);
                        if (folder != null) {
                            if (event.kind() == ENTRY_DELETE) {
                                folder.media.remove(absolutePath.hashCode());
                            } else {
                                MediaType media = cacheService.getEntry(absolutePath.hashCode());
                                if (media == null)
                                    media = new MediaType(absolutePath);
                                folder.media.putIfAbsent(absolutePath.hashCode(), media);
                            }
                            eventObserver.fire(Event.FolderChanged, folderPath.toAbsolutePath().toString(), folder.media);
                        }

                        if (!key.reset())
                            break;
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Watcher error: ", e);
                }
            }
        }).start();
    }

    @Nullable
    private ScanFolder getScanFolder(Path path) {
        for (ScanFolder folder : foldersToScan.values()) {
            if (folder.path.equals(path))
                return folder;
        }
        return null;
    }

    private void scanAllDirectories() {
        foldersToScan.forEach(this::scanDirectory);
    }

    private void scanDirectory(@NotNull String directoryPath, @NotNull ScanFolder folder) {
        if (!folder.scanned) {
            log.debug("Scanning {}", directoryPath);
            visitor.setCurrentFolder(folder);
            folder.media.clear();
            try {
                Files.walkFileTree(folder.path, visitor);
                eventObserver.fire(Event.FolderChanged, directoryPath, folder.media);
            } catch (IOException e) {
                e.printStackTrace();
            }
            folder.scanned = true;
        }
    }

    private class PlayableFileVisitor implements FileVisitor<Path> {

        private ScanFolder folder;

        public void setCurrentFolder(ScanFolder folder) {
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
            if (Arrays.binarySearch(SUPPORTED_FORMATS, absolutePath.toLowerCase().substring(absolutePath.lastIndexOf(DOT) + ONE)) >= ZERO) {
                MediaType media = cacheService.getEntry(absolutePath.hashCode());
                if (media == null)
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

        boolean scanned;

        String absolutePath;

        Path path;

        WatchKey key;

        Map<Integer, MediaType> media;

        ScanFolder(String absolutePath) {
            this.absolutePath = absolutePath;
            media = new LinkedHashMap<>();
            scanned = false;
        }

        ScanFolder annotationToDefinition() throws IOException {
            path = Paths.get(absolutePath);
            key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            return this;
        }

    }

}             */
