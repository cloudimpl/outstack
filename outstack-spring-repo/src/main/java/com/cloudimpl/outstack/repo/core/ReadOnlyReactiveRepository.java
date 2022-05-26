package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Entity;
import com.cloudimpl.outstack.repo.QueryRequest;
import com.cloudimpl.outstack.repo.core.geo.BaseRepository;
import com.cloudimpl.outstack.runtime.ResultSet;
import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReadOnlyReactiveRepository extends BaseRepository {
    <T extends Entity> Mono<T> queryById(String tenantId, Class<T> resourceType, String id);
    <T extends Entity> Flux<T> queryByType(String tenantId, Class<T> resourceType, QueryRequest request);
    <T extends Entity> Mono<ResultSet<T>> queryByTypeWithPagination(String tenantId, Class<T> resourceType, QueryRequest request);
    <T extends Entity> Flux<T> query(String tenantId, QueryRequest request);
    <T extends Entity> Mono<ResultSet<T>> queryWithPagination(String tenantId, QueryRequest req);
    <T extends Entity> Flux<T> findChilds(String tenantId,String tid,String childTenantId,QueryRequest request);
    <T extends Entity> Flux<T> findChildsByType(String tenantId,String tid,String childTenantId,Class<T> resourceType,QueryRequest request);
    <T extends Entity> Flux<T> findChilds(String tenantId,String tid,QueryRequest request);
    <T extends Entity> Flux<T> findChildsByType(String tenantId,String tid,Class<T> resourceType,QueryRequest request);
    <T extends Entity> Flux<T> findParentGraphSearch(String tenantId,Class<? extends Entity> resourceType, String childId, QueryRequest request);
    <T extends Entity> Flux<T> findChildGraphSearch(String tenantId,Class<? extends Entity> resourceType,String parentId,QueryRequest request);
    <T extends Entity> Mono<ResultSet> doPagination(Flux<T> flux, QueryRequest req);
}
