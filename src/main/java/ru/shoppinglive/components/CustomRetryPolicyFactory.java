package ru.shoppinglive.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.netflix.client.Utils;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryContext;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicy;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicyFactory;
import org.springframework.cloud.client.loadbalancer.ServiceInstanceChooser;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Created by rkhabibullin on 14.06.2017.
 */
public class CustomRetryPolicyFactory implements LoadBalancedRetryPolicyFactory {

    private LoadBalancedRetryPolicyFactory springRetryPolicy;
    private LocalRegistryLoadBalancer loadBalancer;

    public CustomRetryPolicyFactory(SpringClientFactory clientFactory, LocalRegistryLoadBalancer loadBalancer) {
        springRetryPolicy = new RibbonLoadBalancedRetryPolicyFactory(clientFactory);
        this.loadBalancer = loadBalancer;
    }

    @Override
    public LoadBalancedRetryPolicy create(String serviceId, ServiceInstanceChooser serviceInstanceChooser) {
        LoadBalancedRetryPolicy policy = springRetryPolicy.create(serviceId, serviceInstanceChooser);
        return new LoadBalancedRetryPolicy() {
            @Override
            public boolean canRetrySameServer(LoadBalancedRetryContext context) {
                return policy.canRetrySameServer(context);
            }

            @Override
            public boolean canRetryNextServer(LoadBalancedRetryContext context) {
                return policy.canRetryNextServer(context);
            }

            @Override
            public void close(LoadBalancedRetryContext context) {
                policy.close(context);
            }

            @Override
            public void registerThrowable(LoadBalancedRetryContext context, Throwable throwable) {
                if(Utils.isPresentAsCause(throwable, ImmutableSet.of(SocketTimeoutException.class, ConnectException.class))){
                    loadBalancer.markServerDown(context.getServiceInstance().getHost(), context.getServiceInstance().getPort());
                }
                policy.registerThrowable(context, throwable);
            }
        };
    }
}
