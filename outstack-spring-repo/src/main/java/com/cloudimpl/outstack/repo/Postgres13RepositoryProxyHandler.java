package com.cloudimpl.outstack.repo;


import com.cloudimpl.outstack.repo.core.ReactiveRepository;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Postgres13RepositoryProxyHandler implements InvocationHandler {

    private final Class<?> targetType;
    private final Object repoInstance;
    public Postgres13RepositoryProxyHandler(Class<?> targetType, AutowireCapableBeanFactory beanFactory)
    {
        this.targetType = targetType;
        if(ReactiveRepository.class.isAssignableFrom(targetType))
        {
            repoInstance = new PostgresReactiveRepository();
        }else {
            repoInstance = new PostgresReadOnlyReactiveRepository();
        }
        PostgresReadOnlyReactiveRepository.class.cast(repoInstance).setTable(RepoUtil.getRepoMeta(this.targetType,true));
        beanFactory.autowireBean(repoInstance);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(repoInstance, args);
    }
}
