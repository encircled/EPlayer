package cz.encircled.eplayer.model;

import java.util.Date;

/**
 * @author Kisel on 13.08.2015.
 */
public class MediaTabType {

    private long id;

    private String path;

    public void generateNewId() {
        id = new Date().getTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
