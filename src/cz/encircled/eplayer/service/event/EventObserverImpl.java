package cz.encircled.eplayer.service.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Encircled on 13/09/2014.
 */
@Resource
public class EventObserverImpl implements EventObserver {

    private static final Logger log = LogManager.getLogger();

    private final Map<Event, List<EventListener>> events;

    public EventObserverImpl() {
        events = new HashMap<>();
    }

    @Override
    public void fire(Event event, Object... args) {
        log.debug("Fire event {} with args {}", event, args);
        checkEvent(event);
        events.get(event).parallelStream().forEach(l -> l.handle(event, args));
    }

    @Override
    public void listen(Event event, EventListener eventListener) {
        log.debug("Add listener to {} event", event);
        checkEvent(event);
        events.get(event).add(eventListener);
    }

    private void checkEvent(Event event) {
        if (!events.containsKey(event))
            events.put(event, new ArrayList<>());
    }

}
