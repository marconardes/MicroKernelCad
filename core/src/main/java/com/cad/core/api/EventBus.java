package com.cad.core.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * EventBus is a simple event bus implementation that allows for the registration of listeners
 * and the publishing of events to those listeners.
 *
 * This class uses generics to allow for type-safe event handling.
 */
public class EventBus {
    

    // Mapeia tipos de evento para conjuntos de ouvintes
    private final Map<Class<?>, Set<Consumer<?>>> listeners = new HashMap<>();

    // Método para registrar um ouvinte para um tipo de evento específico
    public <T> void registerListener(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new HashSet<>()).add(listener);
    }

    // Método para publicar um evento para todos os ouvintes registrados desse tipo
    public <T> void publishEvent(T event) {
        Set<Consumer<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (Consumer<?> listener : eventListeners) {
                @SuppressWarnings("unchecked")
                Consumer<T> typedListener = (Consumer<T>) listener;
                typedListener.accept(event);
            }
        }
    }
}
