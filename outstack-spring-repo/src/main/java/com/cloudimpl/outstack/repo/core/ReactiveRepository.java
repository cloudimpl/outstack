package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveRepository extends ReadOnlyReactiveRepository{
    <T extends Entity> Mono<T> create(String tenantId, T entity);
    <T extends Entity> Mono<T> createOrUpdate(String tenantId,T entity);
    <T extends Entity> Flux<T> createOrUpdate(String tenantId, Collection<T> entities);
    <T extends Entity> Mono<T> delete(String tenantId,Class<T> resourceType,String id);
    <T extends Entity> Mono<T> delete(String tenantId,String tid);
    <T extends Entity> Mono<T> createChild(String parentTenantId,String parentTid,String tenantId,T child);
    <T extends Entity> Mono<T> update(String tenantId, T entity, String id);
    <T extends Entity> Mono<T> updateChild(String parentTid, String tenantId, T child, String id);
    <T extends Entity> Mono<T> convertToChild(String tenantId, String parentTid, String id, Class<T> resourceType);

}
