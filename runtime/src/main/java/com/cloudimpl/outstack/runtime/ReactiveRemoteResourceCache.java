/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.runtime;

import com.amazonaws.retry.RetryUtils;
import com.cloudimpl.outstack.runtime.util.Util;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.util.concurrent.CompleteFuture;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;
import reactor.retry.RetryContext;

/**
 *
 * @author nuwan
 */
public class ReactiveRemoteResourceCache {

    private final Cache<String, CacheItem> map;
    private final Map<String, Disposable> mapDisposable;
    private final BiFunction<String, Object, Flux> requestStream;
    private final String serviceName;

    public ReactiveRemoteResourceCache(int maxSize, Duration evictionDuration, String serviceName, BiFunction<String, Object, Flux> requestStream) {
        map = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(evictionDuration)
                .evictionListener(this::onRemove)
                .build();
        mapDisposable = new ConcurrentHashMap<>();
        this.requestStream = requestStream;
        this.serviceName = serviceName;
    }

    public <T> Mono<T> request(String key, Object req, Predicate<? super RetryContext<T>> retryOn) {
        CacheItem<T> item = map.get(key, k -> new CacheItem<>());
        synchronized (item) {
            if (item.item != null) {
                return Mono.just(item.item);
            } else {
                CompletableFuture<T> future = new CompletableFuture<>();
                item.addFuture(future);
                if (item.hnd == null) {
                    item.hnd = requestStream.apply(this.serviceName, req).retryWhen(Util.wrap(Retry
                            .onlyIf(retryOn)
                            .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofSeconds(20)))
                    )
                            .publishOn(Schedulers.parallel())
                            .doOnNext(s -> {
                                item.completeFutures((T) s);
                            })
                            .doOnTerminate(() -> item.completeFuturesExceptionally(new RuntimeException("connection closed")))
                            .doOnCancel(() -> item.completeFuturesExceptionally(new RuntimeException("connection cancelled")))
                            .subscribe();
                }

                return Mono.fromFuture(future);
            }
        }
    }

    private void onRemove(String key, CacheItem item) {
            item.close();
    }

    public static final class CacheItem<T> {

        private T item;
        private Disposable hnd;
        private final Queue<CompletableFuture> futures = new ConcurrentLinkedQueue<>();

        public CacheItem() {
            this.hnd = null;
        }

        public void close() {
            synchronized (this) {
                this.hnd.dispose();
                this.hnd = null;
                this.item = null;
                completeFuturesExceptionally(new RuntimeException("item closed"));
            }
        }

        public void addFuture(CompletableFuture future) {
            this.futures.add(future);
        }

        public void completeFutures(T item) {
            synchronized (this) {
                this.item = item;
                while (!futures.isEmpty()) {
                    CompletableFuture future = futures.poll();
                    future.complete(item);
                }
                this.item = null;
            }

        }

        public void completeFuturesExceptionally(Throwable thr) {
            synchronized (this) {
                while (!futures.isEmpty()) {
                    CompletableFuture future = futures.poll();
                    future.completeExceptionally(thr);
                }
                this.item = null;
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReactiveRemoteResourceCache cache = new ReactiveRemoteResourceCache(10000, Duration.ofSeconds(5), "test", (s, o) -> Flux.interval(Duration.ofSeconds(1)));
        while (true) {
            Thread.sleep(2000);
            cache.request("1", "asf", c -> true).subscribe(System.out::println);
        }
    }
}
