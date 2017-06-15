package ru.shoppinglive.components;

import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

/**
 * Created by rkhabibullin on 14.06.2017.
 */
public class LocalRegistryRouteLocator extends SimpleRouteLocator {
    public LocalRegistryRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
    }
}
