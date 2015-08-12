package cz.encircled.eplayer.service.event;

/**
 * @author Encircled on 13/09/2014.
 */
public interface EventObserver {

    void fire(Event<Void, Void> event);

    <A> void fire(Event<A, Void> event, A arg);

    <A, A2> void fire(Event<A, A2> event, A arg, A2 arg2);

    <A, A2> void listen(Event<A, A2> event, EventListener<A, A2> eventListener);

    <A, A2> void listenFxThread(Event<A, A2> event, EventListener<A, A2> eventListener);

}
