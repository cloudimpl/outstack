package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Event;
import com.cloudimpl.outstack.repo.QueryRequest;
import com.cloudimpl.outstack.repo.core.geo.BaseRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReadOnlyReactiveEventRepository extends BaseRepository {

    <T extends Event> Flux<T> queryEvents(String tenantId,QueryRequest queryRequest);
    <T extends Event> Flux<T> queryEvents(String tenantId,Class<T> eventType,QueryRequest queryRequest);
    <T extends Event> Mono<T> queryEventById(String tenantId,Class<T> eventType, String id);
}
