package cz.encircled.eplayer.model;

import cz.encircled.eplayer.core.ApplicationCore;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class MediaFile {

    private static final String TO_STRING_FORMAT = "Playable name: %s, path: %s";

    private String path;

    private String name;

    private long size;

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

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public boolean exists() {
        return path != null && new java.io.File(path).exists();
    }

    @Override
    public String toString() {
        return String.format(TO_STRING_FORMAT, name, path);
    }

    public void updatePath(@NotNull String path) {
        File file = new File(path);
        this.path = path;

        String fullName = file.getName();
        int lastDot = fullName.lastIndexOf(".");
        if (lastDot > -1) {
            name = fullName.substring(0, lastDot);
        } else {
            name = fullName;
        }

        size = file.length();
        pathToScreenshot = ApplicationCore.getScreenshotURL(this);
    }
}
