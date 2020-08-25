package cz.encircled.eplayer.service.event;

/**
 * @author Encircled on 13/09/2014.
 */
public interface EventObserver {

    void fire(Event<Void> event);

    <A> void fire(Event<A> event, A arg);

    <A> void listen(Event<A> event, EventListener<A> eventListener);

    <A> void listenFxThread(Event<A> event, EventListener<A> eventListener);

}
