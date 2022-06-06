package com.cloudimpl.outstack.repo.core.geo;

import com.cloudimpl.outstack.repo.QueryRequest;
import reactor.core.publisher.Flux;

public interface ReactiveGeoQueryRepository {
    <T> Flux<T> findEntitiesWithinBoundaryWithType(String tenantId,Class<T> type, Polygon polygon, QueryRequest request);
    <T> Flux<T> findEntitiesWithinBoundary(String tenantId, Polygon polygon, QueryRequest request);
    <T> Flux<T> findClosestBoundEntitiesForPoint(String tenantId,Point point,QueryRequest request);
    <T> Flux<T> findClosestBoundEntitiesForPointWithType(String tenantId,Class<T> type,Point point,QueryRequest request);
}
