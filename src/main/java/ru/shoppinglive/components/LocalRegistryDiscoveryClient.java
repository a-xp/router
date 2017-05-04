package ru.shoppinglive.components;

import com.google.common.collect.ImmutableList;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netflix.appinfo.InstanceInfo.PortType.SECURE;

/**
 * Created by rkhabibullin on 04.05.2017.
 */
@Component
@Primary
public class LocalRegistryDiscoveryClient implements DiscoveryClient {
    @Autowired
    private EurekaInstanceConfig config;
    @Autowired
    private PeerAwareInstanceRegistry instanceRegistry;
    @Autowired
    @Lazy
    private ZuulHandlerMapping mapping;
    private static Logger logger = LoggerFactory.getLogger(LocalRegistryDiscoveryClient.class);
    private ApplicationEvent lastEvent;

    @Override
    public String description() {
        return "LOCAL EUREKA REGISTRY DISCOVERY CLIENT";
    }

    public ServiceInstance getLocalServiceInstance() {
        return new ServiceInstance() {
            @Override
            public String getServiceId() {
                return LocalRegistryDiscoveryClient.this.config.getAppname();
            }

            @Override
            public String getHost() {
                return LocalRegistryDiscoveryClient.this.config.getHostName(false);
            }

            @Override
            public int getPort() {
                return LocalRegistryDiscoveryClient.this.config.getNonSecurePort();
            }

            @Override
            public boolean isSecure() {
                return LocalRegistryDiscoveryClient.this.config.getSecurePortEnabled();
            }

            @Override
            public URI getUri() {
                return DefaultServiceInstance.getUri(this);
            }

            @Override
            public Map<String, String> getMetadata() {
                return LocalRegistryDiscoveryClient.this.config.getMetadataMap();
            }
        };
    }

    @Override
    public List<ServiceInstance> getInstances(String s) {
        List<ServiceInstance> instances = ImmutableList.of();
        Application app = instanceRegistry.getApplication(s);
        if(app!=null){
            instances = app.getInstances().stream().filter(ii->ii.getStatus().equals(InstanceInfo.InstanceStatus.UP))
                    .map(ii->new EurekaServiceInstance(new InstanceInfo(ii))).collect(Collectors.toList());
        }
        return instances;
    }

    @Override
    public List<String> getServices() {
        return instanceRegistry.getApplications().getRegisteredApplications().stream()
                .filter(app->{
                    if(lastEvent instanceof EurekaInstanceRegisteredEvent
                            && ((EurekaInstanceRegisteredEvent) lastEvent).getInstanceInfo().getAppName().equals(app.getName()))return true;
                    if(lastEvent instanceof EurekaInstanceCanceledEvent
                            && ((EurekaInstanceCanceledEvent) lastEvent).getAppName().equals(app.getName()))return false;
                    return app.getInstances().stream().anyMatch(ii->ii.getStatus().equals(InstanceInfo.InstanceStatus.UP));
                }).map(app->app.getName().toLowerCase()).collect(Collectors.toList());
    }

    public static class EurekaServiceInstance implements ServiceInstance {
        private InstanceInfo instance;

        EurekaServiceInstance(InstanceInfo instance) {
            this.instance = instance;
        }

        public InstanceInfo getInstanceInfo() {
            return instance;
        }

        @Override
        public String getServiceId() {
            return this.instance.getAppName();
        }

        @Override
        public String getHost() {
            return this.instance.getHostName();
        }

        @Override
        public int getPort() {
            if (isSecure()) {
                return this.instance.getSecurePort();
            }
            return this.instance.getPort();
        }

        @Override
        public boolean isSecure() {
            // assume if secure is enabled, that is the default
            return this.instance.isPortEnabled(SECURE);
        }

        @Override
        public URI getUri() {
            return DefaultServiceInstance.getUri(this);
        }

        @Override
        public Map<String, String> getMetadata() {
            return this.instance.getMetadata();
        }
    }

    @EventListener
    public void onRegister(EurekaInstanceRegisteredEvent event){
        lastEvent = event;
        logger.info("Service "+event.getInstanceInfo().getAppName()+" registered. Updating zuul mapping");
        mapping.setDirty(true);
    }

    @EventListener
    public void onCancel(EurekaInstanceCanceledEvent event){
        lastEvent = event;
        logger.info("Service "+event.getAppName()+" canceled. Updating zuul mapping");
        mapping.setDirty(true);
    }
}
