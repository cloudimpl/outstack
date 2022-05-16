package com.cloudimpl.outstack.repo.postgres;


import com.cloudimpl.outstack.repo.RepoUtil;
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
            repoInstance = new Postgres13ReactiveRepository();
        }else {
            repoInstance = new Postgres13ReadOnlyReactiveRepository();
        }
        Postgres13ReadOnlyReactiveRepository.class.cast(repoInstance).setTable(RepoUtil.getRepoMeta(this.targetType,true));
        beanFactory.autowireBean(repoInstance);
        Postgres13ReadOnlyReactiveRepository.class.cast(repoInstance).init();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(repoInstance, args);
    }
}
