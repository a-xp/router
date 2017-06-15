package ru.shoppinglive.components;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.client.config.IClientConfig;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.loadbalancer.AbstractLoadBalancer;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by rkhabibullin on 04.05.2017.
 */

public class LocalRegistryLoadBalancer extends AbstractLoadBalancer {
    private PeerAwareInstanceRegistry instanceRegistry;
    private EurekaInstanceConfig eurekaInstanceConfig;
    private IClientConfig clientConfig;
    protected LoadBalancerStats lbStats = new LoadBalancerStats("default");
    private String appName;

    public LocalRegistryLoadBalancer(PeerAwareInstanceRegistry instanceRegistry, IClientConfig clientConfig, EurekaInstanceConfig eurekaInstanceConfig) {
        this.instanceRegistry = instanceRegistry;
        this.clientConfig = clientConfig;
        this.eurekaInstanceConfig = eurekaInstanceConfig;
        appName = clientConfig.getClientName().toUpperCase();
    }

    static final Logger logger = LoggerFactory.getLogger(LocalRegistryLoadBalancer.class);

    @Override
    public List<Server> getServerList(ServerGroup serverGroup) {
        logger.warn("Calling not implemented method LocalRegistryLoadBalancer.getServerList");
        return Collections.emptyList();
    }

    @Override
    public LoadBalancerStats getLoadBalancerStats() {
        return lbStats;
    }

    @Override
    public void addServers(List<Server> newServers) {
        logger.warn("LocalRegistryLoadBalancer.addServers to NoOpLoadBalancer ignored");
    }

    @Override
    public Server chooseServer(Object key) {
        Server s = instanceRegistry.getApplication(appName).getInstances().stream()
            .filter(ii-> ii.getStatus()== InstanceInfo.InstanceStatus.UP)
            .sorted(this::compareInstances)
            .map(ii->new DiscoveryEnabledServer(ii, false, true))
            .findFirst().orElseGet(()->null);
        return s;
    }

    public void markServerDown(String host, int port){
        for(InstanceInfo ii: instanceRegistry.getApplication(appName).getInstances()){
            if(ii.getIPAddr().equals(host) && ii.getPort()==port){
                ii.setStatus(InstanceInfo.InstanceStatus.DOWN);
            }
        }
    }

    @Override
    public void markServerDown(Server server) {
        logger.warn("LocalRegistryLoadBalancer.markServerDown to NoOpLoadBalancer ignored");
    }

    @Override
    public List<Server> getServerList(boolean availableOnly) {
        return Collections.emptyList();
    }

    @Override
    public List<Server> getReachableServers() {
        return null;
    }

    @Override
    public List<Server> getAllServers() {
        return null;
    }

    protected byte compareInstances(InstanceInfo i1, InstanceInfo i2){
        String localIp = eurekaInstanceConfig.getIpAddress();
        if(!i1.getIPAddr().equals(localIp))return 1;
        if(!i2.getIPAddr().equals(localIp))return -1;
        long start1 = Long.parseLong(i1.getMetadata().get("startTime"));
        long start2 = Long.parseLong(i2.getMetadata().get("startTime"));
        return (byte)(start1==start2?0:(start1<start2?1:-1));
    }
}
