package cz.encircled.eplayer.model;

import cz.encircled.eplayer.core.ApplicationCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class MediaFile {

    private static final String TO_STRING_FORMAT = "Playable name: %s, path: %s";

    private String path;

    private String name;

    private String extension;

    private long size;

    private long fileCreationDate;

    private String pathToScreenshot;

    public MediaFile(@NotNull String path) {
        updatePath(path);
    }

    public String getPathToScreenshot() {
        return pathToScreenshot;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public String getExtension() {
        return extension;
    }

    public long getSize() {
        return size;
    }

    public boolean exists() {
        return path != null && new java.io.File(path).exists();
    }

    public long getFileCreationDate() {
        return fileCreationDate;
    }


    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, name, path);
    }

    public void updatePath(@NotNull String path) {
        File file = new File(path);
        this.path = path;

        try {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getPath()), BasicFileAttributes.class);
            this.fileCreationDate = attr.creationTime().toMillis();
        } catch (IOException e) {
            this.fileCreationDate = 0L;
            e.printStackTrace();
            // TODO
        }

        String fullName = file.getName();
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot > -1) {
            name = fullName.substring(0, lastDot);
            extension = fullName.substring(lastDot + 1, fullName.length());
        } else {
            name = fullName;
            extension = "";
        }

        size = file.length();
        pathToScreenshot = ApplicationCore.getScreenshotURL(this);
    }
}
