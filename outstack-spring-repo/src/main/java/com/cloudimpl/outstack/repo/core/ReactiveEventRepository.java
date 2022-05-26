package com.cloudimpl.outstack.repo.core;

import com.cloudimpl.outstack.repo.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveEventRepository extends ReadOnlyReactiveEventRepository{
    <T extends Event> Mono<T> addEvent(String tenantId, T event);
    <T extends Event> Flux<T> addEvents(String tenantId, Collection<T> events);
    <T extends Event> Mono<T> updateEvent(String tenantId,String eventId, T event);
    <T extends Event> Mono<T> deleteEvent(String tenantId,Class<T> eventType,String eventId);
}
