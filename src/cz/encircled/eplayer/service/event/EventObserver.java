package cz.encircled.eplayer.service.event;

/**
 * Created by Encircled on 13/09/2014.
 */
public interface EventObserver {

    void fire(Event event, Object... args);

    void listen(Event event, EventListener eventListener);

}
