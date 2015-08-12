package cz.encircled.eplayer.service.event;

import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.TrackDescription;

import java.util.List;

/**
 * @author Encircled on 13/09/2014.
 */
public class Event<A, A2> {

    @NotNull
    public static Event<Void, Void> contextInitialized = new Event<>("contextInitialized");

    @NotNull
    public static Event<Long, Void> mediaTimeChange = new Event<>("mediaTimeChange");

    @NotNull
    public static Event<List<TrackDescription>, Void> subtitlesUpdated = new Event<>("subtitlesUpdated");

    @NotNull
    public static Event<List<TrackDescription>, Void> audioTracksUpdated = new Event<>("audioTracksUpdated");

    // True if playing
    @NotNull
    public static Event<Boolean, Void> playingChanged = new Event<>("playingChanged");

    @NotNull
    public static Event<Long, Void> mediaDurationChange = new Event<>("mediaDurationChange");

    private String name;

    public Event(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
}
