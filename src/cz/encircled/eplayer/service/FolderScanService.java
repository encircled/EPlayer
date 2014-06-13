package cz.encircled.eplayer.service;

/**
 * Created by Encircled on 11/06/2014.
 */
public interface FolderScanService {

    void stop();

    FolderScanService addFiledScanListener(FileScanListener listener);

    FolderScanService addAllIfAbsent(String[] absolutePaths);

    boolean removeFolder(String absolutePath);

    boolean addIfAbsent(String absolutePath);

    FolderScanService initialize();

    void start();

    // TODO not nice
    void scanDirectories();
}
