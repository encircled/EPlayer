package cz.encircled.eplayer.service.event;

/**
 * Created by Encircled on 13/09/2014.
 */
public interface EventListener {

    void handle(Event event, Object... args);

}
