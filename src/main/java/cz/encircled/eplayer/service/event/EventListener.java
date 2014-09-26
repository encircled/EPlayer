package cz.encircled.eplayer.service.event;

/**
 * Created by Encircled on 13/09/2014.
 */
public interface EventListener<A, A2> {

    void handle(Event event, A arg, A2 arg2);

}
