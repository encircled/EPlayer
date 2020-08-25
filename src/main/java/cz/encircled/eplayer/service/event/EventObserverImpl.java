package cz.encircled.eplayer.service.event;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Encircled on 13/09/2014.
 */
public class EventObserverImpl implements EventObserver {

    private static final Logger log = LogManager.getLogger();

    @NotNull
    private final Map<Event, List<EventListener>> events;

    @NotNull
    private final Map<Event, List<EventListener>> fxEvents;

    public EventObserverImpl() {
        events = new HashMap<>();
        fxEvents = new HashMap<>();
    }

    @Override
    public <A> void listen(Event<A> event, EventListener<A> eventListener) {
        log.debug("Add listener to {}", event);
        checkEvent(event);
        events.get(event).add(eventListener);
    }

    @Override
    public <A> void listenFxThread(Event<A> event, EventListener<A> eventListener) {
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
    public void fire(Event<Void> event) {
        fire(event, null);
    }

    @Override
    public <A> void fire(Event<A> event, A arg) {
        log.debug("Fire event {} with arg {}) ", event, arg);
        checkEvent(event);
        checkFxEvent(event);
        events.get(event).forEach(l -> l.handle(arg));
//        Platform.runLater(() -> fxEvents.get(event).stream().forEach(l -> l.handle(event, arg, arg2)));
        fxEvents.get(event).forEach(l -> Platform.runLater(() -> l.handle(arg)));
    }

}
