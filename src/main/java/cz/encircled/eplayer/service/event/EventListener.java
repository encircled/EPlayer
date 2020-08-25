package cz.encircled.eplayer.service.event;

/**
 * @author Encircled on 13/09/2014.
 */
public interface EventListener<A> {

    void handle(A arg);

}
