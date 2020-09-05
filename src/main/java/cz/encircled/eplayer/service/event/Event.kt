package cz.encircled.eplayer.service.event;

import cz.encircled.eplayer.model.GenericTrackDescription;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Encircled on 13/09/2014.
 */
public class Event<A> {

    @NotNull
    public static Event<Void> contextInitialized = new Event<>("contextInitialized");

    @NotNull
    public static Event<Long> mediaTimeChange = new Event<>("mediaTimeChange");

    @NotNull
    public static Event<List<GenericTrackDescription>> subtitlesUpdated = new Event<>("subtitlesUpdated");

    @NotNull
    public static Event<List<GenericTrackDescription>> audioTracksUpdated = new Event<>("audioTracksUpdated");

    // True if playing
    @NotNull
    public static Event<Boolean> playingChanged = new Event<>("playingChanged");

    @NotNull
    public static Event<Long> mediaDurationChange = new Event<>("mediaDurationChange");

    private final String name;
    public final boolean verbose;

    public Event(String name) {
        this.name = name;
        this.verbose = !name.equals("mediaTimeChange");
    }

    @NotNull
    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
}
