package cz.encircled.eplayer.model;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created by Encircled on 23/09/2014.
 */
@Resource
public class SeriesVideo {

    private static final Comparator<MediaType> mediaTypeComparator = (o1, o2) -> o1.getName().compareTo(o2.getName());

    private String name;

    private TreeMap<MediaType, Void> mediaTypes;

    public SeriesVideo() {
        mediaTypes = new TreeMap<>(mediaTypeComparator);
    }

    @Nullable
    public MediaType getLast() {
        if (mediaTypes.size() == 0)
            return null;

        for (MediaType mediaType : mediaTypes.descendingKeySet()) {
            if (mediaType.getTime() > 0)
                return mediaType;
        }

        return mediaTypes.firstKey();
    }

    @Nullable
    public MediaType getNext() {
        if (mediaTypes.size() == 0)
            return null;
        if (mediaTypes.size() == 1)
            return mediaTypes.firstKey();

        System.out.println("s" + mediaTypes.size());

        MediaType previous = null;
        for (MediaType mediaType : mediaTypes.descendingKeySet()) {
            if (mediaType.getTime() > 0) {
                System.out.println("Previous is " + previous);
                return previous;
            }
            previous = mediaType;
        }

        return mediaTypes.firstKey();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeMap<MediaType, Void> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(TreeMap<MediaType, Void> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public void addMediaType(MediaType mediaType) {
        mediaTypes.put(mediaType, null);
    }

    public static void main(String[] args) {
        SeriesVideo s = new SeriesVideo();
        MediaType m = new MediaType("D:/1.avi");
        MediaType m2 = new MediaType("D:/2.avi");
        MediaType m3 = new MediaType("D:/3.avi");
        m.setTime(1);
        m2.setTime(1);
        m3.setTime(0);
        s.addMediaType(m);
        s.addMediaType(m2);
        s.addMediaType(m3);
        System.out.println(s.getLast());
        System.out.println(" next " + s.getNext());
    }

}
