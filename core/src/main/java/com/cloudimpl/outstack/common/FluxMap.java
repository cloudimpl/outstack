/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.common;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author nuwan
 */
public class FluxMap<K, V> {

    private final Map<K, V> map;
    private final SingleFluxProcessor<Item<K, V>> itemProcessor;
    //private final Flux<Event<K, V>> flux;
    private final FluxProcessor<Event<K, V>> publisher;
    public static final Scheduler defaultSched = Schedulers.newSingle("fluxmap",true);
    public final Scheduler sched;
    public FluxMap(String name){
        this(name,defaultSched);
    }
    
    public FluxMap(String name,Scheduler scheduler) {
        this.sched = scheduler;
        map = new ConcurrentHashMap<>();
        itemProcessor = new SingleFluxProcessor<>(this::onEmitter);
        itemProcessor.flux().subscribeOn(scheduler).doOnNext(this::onItem).subscribe();
        publisher = new FluxProcessor<>(name,sink->map.entrySet().forEach(e->{
            sink.next(new Event<>(Event.Type.ADD, e.getKey(), e.getValue()));} ));
       // flux = Flux.fromIterable(map.entrySet()).subscribeOn(scheduler).map(e -> new Event<>(Event.Type.ADD, e.getKey(), e.getValue())).concatWith(publisher.flux().publishOn(Schedulers.parallel()));
    }

    public Flux<Event<K, V>> flux(String subscriber) {
        return publisher.flux(subscriber).subscribeOn(sched);
    }

    public Collection<V> values() {
        return map.values();
    }
    
    public Mono<V> putIfAbsent(K key, V value) {
        CompletableFuture<V> future = new CompletableFuture<>();
        Mono<V> mono = Mono.fromFuture(future);
        itemProcessor.send(new Item(future, Event.Type.ADD, key, value, true));
        return mono;
    }

    public Mono<V> put(K key, V value) {
        CompletableFuture<V> future = new CompletableFuture<>();
        Mono<V> mono = Mono.fromFuture(future);
        itemProcessor.send(new Item(future, Event.Type.ADD, key, value, false));
        return mono;
    }

    public Mono<V> remove(K key) {
        CompletableFuture<V> future = new CompletableFuture<>();
        Mono<V> mono = Mono.fromFuture(future);
        itemProcessor.send(new Item(future, Event.Type.REMOVE, key, null, false));
        return mono;
    }

    public V get(K key)
    {
        return map.get(key);
    }
    
    private void onEmitter() {

    }

    private void onItem(Item<K, V> item) {
        switch (item.action) {
            case ADD -> {
                V v;
                if (item.isPutIfAbsent()) {
                    v = map.putIfAbsent(item.key, item.value);
                    item.future.complete(v);
                } else {
                    v = map.put(item.key, item.value);
                    item.future.complete(v);
                }
                if (v != null) {
                    publisher.add(new Event<>(Event.Type.UPDATE, item.key, item.value));
                } else {
                    publisher.add(new Event<>(Event.Type.ADD, item.key, item.value));
                }
            }
            case REMOVE -> {
                V v = map.remove(item.key);
                item.future.complete(v);
                if (v != null) {
                    publisher.add(new Event<>(Event.Type.REMOVE, item.key, v));
                }
            }
            case UPDATE -> {

            }
            default -> {
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FluxMap<String, String> map = new FluxMap<>("fluxmap",Schedulers.newSingle("xxxx"));
        map.put("key1", "value1");
        map.put("key2", "value2");
//        map.put("key3", "value1");
        Disposable hnd1 = map.flux("test1").doOnNext(e -> {
            System.out.println(Thread.currentThread().getName() + ":" + e);
            throw new RuntimeException("xxx");
        }).subscribe();
        Disposable hnd2 = map.flux("test2").doOnNext(e -> System.out.println(Thread.currentThread().getName() + "->" + e)).subscribe();
        Thread.sleep(3000);
        map.put("key3", "value3");
        //Disposable hnd3 = Flux.interval(Duration.ofSeconds(1)).flatMap(i->map.put("bar"+i, "i"+i).switchIfEmpty(Mono.just("emptydddd").doOnNext(e->System.out.println(e)))).subscribe();
        Thread.sleep(10000);
        map.remove("key1");
        hnd1.dispose();
        hnd2.dispose();
    //    hnd3.dispose();
        Thread.sleep(10000);
    }

    public static final class Item<K, V> {

        private final Event.Type action;
        private final CompletableFuture<V> future;
        private final K key;
        private final V value;
        private final boolean putIfAbsent;

        public Item(CompletableFuture<V> future, Event.Type action, K key, V value, boolean putIfAbsent) {
            this.action = action;
            this.future = future;
            this.key = key;
            this.putIfAbsent = putIfAbsent;
            this.value = value;
        }

        public Event.Type getAction() {
            return action;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public CompletableFuture<V> getFuture() {
            return future;
        }

        public boolean isPutIfAbsent() {
            return putIfAbsent;
        }

    }

    public static class Event<K, V> {

        public enum Type {
            ADD,
            UPDATE,
            REMOVE
        }

        private final Type type;
        private final K key;
        private final V value;

        public Event(Type type, K key, V value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Event{" + "type=" + type + ", key=" + key + ", value=" + value + '}';
        }

    }
}
