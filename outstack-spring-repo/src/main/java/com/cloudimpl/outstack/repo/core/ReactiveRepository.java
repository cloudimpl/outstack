package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Entity;
import reactor.core.publisher.Mono;

public interface ReactiveRepository extends ReadOnlyReactiveRepository{
    <T extends Entity> Mono<T> create(String tenantId, T entity);
    <T extends Entity> Mono<T> createOrUpdate(String tenantId,T entity);
    Mono<Void> delete(String tenantId,Class<? extends Entity> resourceType,String id);
    <T extends Entity> Mono<T> createChild(String parentTenantId,String parentTid,String tenantId,T child);
}
