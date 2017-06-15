package ru.shoppinglive.components;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.resources.ServerCodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistry;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistryProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Created by rkhabibullin on 04.05.2017.
 */
@Component
@Primary
public class CustomInstanceRegistry extends InstanceRegistry {

    @Autowired
    @Lazy
    protected DiscoveryClientRouteLocator routeLocator;

    static final Logger logger = LoggerFactory.getLogger(CustomInstanceRegistry.class);

    public CustomInstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig,
                                  ServerCodecs serverCodecs, EurekaClient eurekaClient, InstanceRegistryProperties instanceRegistryProperties) {
        super(serverConfig, clientConfig, serverCodecs, eurekaClient,
                instanceRegistryProperties.getExpectedNumberOfRenewsPerMin(),
                instanceRegistryProperties.getDefaultOpenForTrafficCount());
    }

    @Override
    public void register(InstanceInfo info, int leaseDuration, boolean isReplication) {
        if(!info.getMetadata().containsKey("startTime"))
            info.getMetadata().put("startTime", String.valueOf(System.currentTimeMillis()));
        super.register(info, leaseDuration, isReplication);
        logger.info("Refreshing route locator "+routeLocator.getClass().getName());
        routeLocator.refresh();
    }

    @Override
    public void register(InstanceInfo info, boolean isReplication) {
        if(!info.getMetadata().containsKey("startTime"))
            info.getMetadata().put("startTime", String.valueOf(System.currentTimeMillis()));
        super.register(info, isReplication);
        logger.info("Refreshing route locator "+routeLocator.getClass().getName());
        routeLocator.refresh();
    }

    @Override
    protected boolean internalCancel(String appName, String id, boolean isReplication) {
        boolean result = super.internalCancel(appName, id, isReplication);
        logger.info("Refreshing route locator "+routeLocator.getClass().getName());
        routeLocator.refresh();
        return result;
    }


}
