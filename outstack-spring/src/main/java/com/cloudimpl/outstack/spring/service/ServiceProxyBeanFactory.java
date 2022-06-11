package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.spring.component.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

import static com.cloudimpl.outstack.spring.service.ServiceProxyBeanFactory.SERVICE_PROXY_BEAN_FACTORY;

/**
 * Dynamic proxy instance creation factory
 */
@Component(SERVICE_PROXY_BEAN_FACTORY)
public class ServiceProxyBeanFactory {
    public static final String SERVICE_PROXY_BEAN_FACTORY = "serviceProxyBeanFactory";

    @Autowired
    private Cluster cluster;


    public ServiceProxyBeanFactory() {
    }

    @SuppressWarnings("unused")
    public <T extends IReactiveService> T createServiceProxyBean(Class<T> beanClass) {

        return (T)Proxy.newProxyInstance(beanClass.getClassLoader(),new Class[]{beanClass},new ReactiveServiceProxyHandler(beanClass,cluster));
    }
}