package ru.shoppinglive.components;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.resources.ServerCodecs;
import org.springframework.beans.BeansException;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistry;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistryProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Created by rkhabibullin on 04.05.2017.
 */
@Component
@Primary
public class CustomInstanceRegistry extends InstanceRegistry {

    protected ApplicationContext ctxt;

    public CustomInstanceRegistry(EurekaServerConfig serverConfig, EurekaClientConfig clientConfig,
                                  ServerCodecs serverCodecs, EurekaClient eurekaClient, InstanceRegistryProperties instanceRegistryProperties) {
        super(serverConfig, clientConfig, serverCodecs, eurekaClient,
                instanceRegistryProperties.getExpectedNumberOfRenewsPerMin(),
                instanceRegistryProperties.getDefaultOpenForTrafficCount());
    }

    @Override
    public void register(InstanceInfo info, int leaseDuration, boolean isReplication) {
        super.register(info, leaseDuration, isReplication);
        ctxt.publishEvent(new RegistryChangedEvent(this));
    }

    @Override
    public void register(InstanceInfo info, boolean isReplication) {
        super.register(info, isReplication);
        ctxt.publishEvent(new RegistryChangedEvent(this));
    }

    @Override
    public boolean cancel(String appName, String serverId, boolean isReplication) {
        boolean result = super.cancel(appName, serverId, isReplication);
        ctxt.publishEvent(new RegistryChangedEvent(this));
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        super.setApplicationContext(context);
        this.ctxt = context;
    }
}
