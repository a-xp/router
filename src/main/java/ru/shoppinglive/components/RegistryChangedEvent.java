package ru.shoppinglive.components;

import org.springframework.context.ApplicationEvent;

/**
 * Created by rkhabibullin on 04.05.2017.
 */
public class RegistryChangedEvent extends ApplicationEvent {
    public RegistryChangedEvent(Object source) {
        super(source);
    }
}
