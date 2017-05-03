package ru.shoppinglive.components;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistry;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by rkhabibullin on 04.04.2017.
 */
@Component
public class ServiceVersionWatcher {
    @Value("${registry.shutdown-old}")
    private boolean enabled = true;
    @Autowired
    private PeerAwareInstanceRegistry instanceRegistry;
    @Autowired
    private RestTemplate restTemplate;

    private static Logger logger = LoggerFactory.getLogger("application");

    @EventListener
    public void onInstanceUp(EurekaInstanceRegisteredEvent event){
        if(!enabled)return;
        InstanceInfo ii = event.getInstanceInfo();
        String id = ii.getInstanceId();
        System.out.println("UP "+id);
        Long refStartTime = Long.parseLong(ii.getMetadata().get("start-time"));
        String appName = ii.getAppName();
        Application app = instanceRegistry.getApplication(appName);
        if(app!=null) {
            app.getInstances().forEach(instanceInfo -> {
                if (!instanceInfo.getId().equals(id)) {
                    Long startTime = Long.parseLong(instanceInfo.getMetadata().get("start-time"));
                    if(startTime<refStartTime){
                        instanceInfo.setStatus(InstanceInfo.InstanceStatus.OUT_OF_SERVICE);
                        try {
                            restTemplate.postForLocation("http://localhost:" + instanceInfo.getPort() + "/shutdown", null);
                        }catch (Exception e){
                            logger.info("Failed to shutdown service "+instanceInfo.getAppName()+" "+instanceInfo.getInstanceId(), e);
                        }
                        System.out.println("Closing " + instanceInfo.getInstanceId());
                    }
                }
            });
        }
    }

}
