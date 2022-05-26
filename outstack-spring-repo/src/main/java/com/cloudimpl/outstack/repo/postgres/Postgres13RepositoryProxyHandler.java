package com.cloudimpl.outstack.repo.postgres;


import com.cloudimpl.outstack.repo.RepoUtil;
import com.cloudimpl.outstack.repo.core.ReactiveEventRepository;
import com.cloudimpl.outstack.repo.core.ReactiveRepository;
import com.cloudimpl.outstack.repo.core.ReadOnlyReactiveRepository;
import com.cloudimpl.outstack.repo.core.Repository;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Postgres13RepositoryProxyHandler implements InvocationHandler {

    private final Class<?> targetType;
    private final Repository repoInstance;
    public Postgres13RepositoryProxyHandler(Class<?> targetType, AutowireCapableBeanFactory beanFactory)
    {
        this.targetType = targetType;
        if(ReactiveRepository.class.isAssignableFrom(targetType))
        {
            repoInstance = new Postgres13ReactiveRepository();
        }else if(ReadOnlyReactiveRepository.class.isAssignableFrom(targetType)) {
            repoInstance = new Postgres13ReadOnlyReactiveRepository();
        }
        else if(ReactiveEventRepository.class.isAssignableFrom(targetType))
        {
            repoInstance = new Postgres13ReactiveEventRepository();
        }
        else
        {
            repoInstance = new Postgres13ReadOnlyReactiveEventRepository();
        }
        repoInstance.setTable(RepoUtil.getRepoMeta(this.targetType,true));
        beanFactory.autowireBean(repoInstance);
        beanFactory.initializeBean(repoInstance,repoInstance.getClass().getName());
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(repoInstance, args);
    }
}
