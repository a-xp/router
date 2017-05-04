package ru.shoppinglive.components;

import com.google.common.collect.ImmutableList;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.client.config.IClientConfig;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.loadbalancer.ServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by rkhabibullin on 04.05.2017.
 */

public class LocalRegistryServerList implements ServerList<DiscoveryEnabledServer> {

    private IClientConfig clientConfig;
    private PeerAwareInstanceRegistry instanceRegistry;

    public LocalRegistryServerList(IClientConfig clientConfig, PeerAwareInstanceRegistry instanceRegistry) {
        this.clientConfig = clientConfig;
        this.instanceRegistry = instanceRegistry;
    }

    @Override
    public List<DiscoveryEnabledServer> getInitialListOfServers() {
        String serviceId = clientConfig.getClientName().toUpperCase();
        Application app = instanceRegistry.getApplication(serviceId);
        List<DiscoveryEnabledServer> serverList = ImmutableList.of();
        if(app!=null){
            serverList = app.getInstances().stream()
                    .filter(ii->ii.getStatus().equals(InstanceInfo.InstanceStatus.UP))
                    .map(ii->new DiscoveryEnabledServer(new InstanceInfo(ii),false))
                    .collect(Collectors.toList());
        }
        return serverList;
    }

    @Override
    public List<DiscoveryEnabledServer> getUpdatedListOfServers() {
        return getInitialListOfServers();
    }
}
