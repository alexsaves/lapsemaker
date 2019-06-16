package utils;

import java.util.HashSet;
import java.util.Set;

public class EventEmitter<T> {

    private Set<Listener<T>> mCallbacks;

    public  EventEmitter() {
        mCallbacks = new HashSet<>();
    }

    public  void register(Listener<T> cb) {
        mCallbacks.add(cb);
    }

    public  void unregister(Listener<T> cb) {
        mCallbacks.remove(cb);
    }

    /**
     * Fires all registered callbacks
     * @param t
     */
    public  void fire(final T t) {
        final Set<Listener<T>> callbacks = new HashSet<>(mCallbacks);

        for (Listener<T> cb : callbacks) {
            try {
                cb.onEventFired(EventEmitter.this, t);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public interface Listener<T> {
        void onEventFired(EventEmitter<T> emitter, T t);
    }
}
