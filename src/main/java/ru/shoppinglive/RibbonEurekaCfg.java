package ru.shoppinglive;

import com.netflix.client.config.IClientConfig;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.loadbalancer.ServerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.shoppinglive.components.LocalRegistryServerList;

/**
 * Created by rkhabibullin on 04.05.2017.
 */

public class RibbonEurekaCfg {

    @Bean
    public ServerList<?> ribbonServerList(PeerAwareInstanceRegistry instanceRegistry, IClientConfig config){
        return new LocalRegistryServerList(config, instanceRegistry);
    }

}
