package com.cloudimpl.outstack.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

import static com.cloudimpl.outstack.repo.DynamicProxyBeanFactory.DYNAMIC_PROXY_BEAN_FACTORY;


/**
 * Dynamic proxy instance creation factory
 */
@Component(DYNAMIC_PROXY_BEAN_FACTORY)
public class DynamicProxyBeanFactory {
    public static final String DYNAMIC_PROXY_BEAN_FACTORY = "repositoryProxyBeanFactory";

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    private DataSources dataSources;

    public DynamicProxyBeanFactory() {
    }

    @SuppressWarnings("unused")
    public <T> T createDynamicProxyBean(Class<T> beanClass) {
        //noinspection unchecked
        Table table = RepoUtil.getRepoMeta(beanClass,true);
        DataSources.DataSource dataSource = dataSources.getDataSource(table.config());
        if(dataSource.getProvider().equalsIgnoreCase("postgres13"))
        {
            return (T) Proxy.newProxyInstance(beanClass.getClassLoader(), new Class[]{beanClass}, new Postgres13RepositoryProxyHandler(beanClass,beanFactory));
        }else
        {
            throw new RepoException("unknown datasource provider : "+dataSource.getProvider());
        }
    }
}