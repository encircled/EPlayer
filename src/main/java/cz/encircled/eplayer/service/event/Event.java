package cz.encircled.eplayer.service.event;

import uk.co.caprica.vlcj.player.TrackDescription;

import java.util.List;

/**
 * Created by Encircled on 13/09/2014.
 */
public class Event<A, A2> {

    public static Event<Void, Void> contextInitialized = new Event<>("contextInitialized");

    public static Event<Long, Void> mediaTimeChange = new Event<>("mediaTimeChange");

    public static Event<List<TrackDescription>, Void> subtitlesUpdated = new Event<>("subtitlesUpdated");

    public static Event<List<TrackDescription>, Void> audioTracksUpdated = new Event<>("audioTracksUpdated");

    // True if playing
    public static Event<Boolean, Void> playingChanged = new Event<>("playingChanged");

    public static Event<Long, Void> mediaDurationChange = new Event<>("mediaDurationChange");

    private String name;

    public Event(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
}
