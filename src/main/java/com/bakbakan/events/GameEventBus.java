package com.bakbakan.events;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameEventBus {

    private static final GameEventBus INSTANCE = new GameEventBus();

    private final Map<EventType<?>, List<EventHandler<Event>>> handlers = new HashMap<>();

    private GameEventBus() {
    }

    public static GameEventBus getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void register(EventType<T> type, EventHandler<? super T> handler) {
        if (type == null || handler == null) {
            return;
        }
        handlers.computeIfAbsent(type, key -> new ArrayList<>())
                .add((EventHandler<Event>) handler);
    }

    public <T extends Event> void unregister(EventType<T> type, EventHandler<? super T> handler) {
        List<EventHandler<Event>> list = handlers.get(type);
        if (list == null) {
            return;
        }
        list.remove(handler);
        if (list.isEmpty()) {
            handlers.remove(type);
        }
    }

    public void unregisterAll(EventHandler<?> handler) {
        if (handler == null) {
            return;
        }
        handlers.values().forEach(list -> list.remove(handler));
        handlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public <T extends Event> void fire(T event) {
        if (event == null) {
            return;
        }
        List<EventHandler<Event>> list = handlers.get(event.getEventType());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (EventHandler<Event> handler : List.copyOf(list)) {
            if (event.isConsumed()) {
                break;
            }
            handler.handle(event);
        }
    }

    public void clear() {
        handlers.clear();
    }
}
