package ru.shoppinglive;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.client.config.IClientConfig;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.loadbalancer.ILoadBalancer;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import ru.shoppinglive.components.CustomRetryPolicyFactory;
import ru.shoppinglive.components.LocalRegistryLoadBalancer;

/**
 * Created by rkhabibullin on 04.05.2017.
 */

public class RibbonEurekaCfg {


    @Bean
    public ILoadBalancer loadBalancer(IClientConfig config, PeerAwareInstanceRegistry instanceRegistry, EurekaInstanceConfig eurekaInstanceConfig){
        return new LocalRegistryLoadBalancer(instanceRegistry, config, eurekaInstanceConfig);
    }

    @Bean
    @Primary
    public LoadBalancedRetryPolicyFactory customRetryPolicyFactory(SpringClientFactory clientFactory, LocalRegistryLoadBalancer loadBalancer) {
        return new CustomRetryPolicyFactory(clientFactory, loadBalancer);
    }

}
