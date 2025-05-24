package fr.orion.core.event;

import fr.orion.api.event.EventRegistry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleEventRegistry implements EventRegistry {
    private static final Logger log = LoggerFactory.getLogger(SimpleEventRegistry.class);
    private final JDA jda;

    public SimpleEventRegistry(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void registerListener(EventListener listener) {
        this.jda.addEventListener(listener);
        log.info("Registered listener: {}", listener.getClass().getSimpleName());
    }

    @Override
    public void unregisterListener(EventListener listener) {
        this.jda.removeEventListener(listener);
        log.info("Unregistered listener: {}", listener.getClass().getSimpleName());
    }

    @Override
    public void registerListeners(EventListener... listeners) {
        for (EventListener listener : listeners) {
            registerListener(listener);
        }
    }

    @Override
    public void unregisterListeners(EventListener... listeners) {
        for (EventListener listener : listeners) {
            unregisterListener(listener);
        }
    }
}
