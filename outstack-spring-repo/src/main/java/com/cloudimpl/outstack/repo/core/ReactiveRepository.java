package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Entity;
import reactor.core.publisher.Mono;

public interface ReactiveRepository extends ReadOnlyReactiveRepository{
    <T extends Entity> Mono<T> create(String tenantId, T entity);
    <T extends Entity> Mono<T> createOrUpdate(String tenantId,T entity);
    <T extends Entity> Mono<T> delete(String tenantId,Class<T> resourceType,String id);
    <T extends Entity> Mono<T> createChild(String parentTenantId,String parentTid,String tenantId,T child);
    <T extends Entity> Mono<T> update(String tenantId, T entity, String id);
}
