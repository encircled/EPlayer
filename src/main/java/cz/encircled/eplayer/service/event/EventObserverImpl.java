package cz.encircled.eplayer.service.event;

import cz.encircled.elight.core.annotation.Component;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encircled on 13/09/2014.
 */
@Component
public class EventObserverImpl implements EventObserver {

    private static final Logger log = LogManager.getLogger();

    private final Map<Event, List<EventListener>> events;

    private final Map<Event, List<EventListener>> fxEvents;

    public EventObserverImpl() {
        events = new HashMap<>();
        fxEvents = new HashMap<>();
    }

    @Override
    public <A, A2> void listen(Event<A, A2> event, EventListener<A, A2> eventListener) {
        log.debug("Add listener to {}", event);
        checkEvent(event);
        events.get(event).add(eventListener);
    }

    @Override
    public <A, A2> void listenFxThread(Event<A, A2> event, EventListener<A, A2> eventListener) {
        log.debug("Add FX listener to {}", event);
        checkFxEvent(event);
        fxEvents.get(event).add(eventListener);
    }

    private void checkEvent(Event event) {
        if (!events.containsKey(event))
            events.put(event, new ArrayList<>(2));
    }

    private void checkFxEvent(Event event) {
        if (!fxEvents.containsKey(event))
            fxEvents.put(event, new ArrayList<>(2));
    }

    @Override
    public void fire(Event<Void, Void> event) {
        fire(event, null, null);
    }

    @Override
    public <A> void fire(Event<A, Void> event, A arg) {
        fire(event, arg, null);
    }

    @Override
    public <A, A2> void fire(Event<A, A2> event, A arg, A2 arg2) {
        log.debug("Fire event {} with args (arg1:{}, arg2: {}) ", event, arg, arg2);
        checkEvent(event);
        checkFxEvent(event);
        events.get(event).stream().forEach(l -> l.handle(event, arg, arg2));
        fxEvents.get(event).stream().forEach(l -> Platform.runLater(() -> l.handle(event, arg, arg2)));
    }

}
