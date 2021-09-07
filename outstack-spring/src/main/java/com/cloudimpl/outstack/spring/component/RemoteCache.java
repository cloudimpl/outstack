///*
// * Copyright 2021 nuwan.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.cloudimpl.outstack.spring.component;
//
//import com.cloudimpl.outstack.runtime.ReactiveRemoteResourceCache;
//import com.cloudimpl.outstack.runtime.ResourceCache;
//import com.cloudimpl.outstack.runtime.domainspec.Entity;
//import com.cloudimpl.outstack.runtime.repo.RepoStreamingReq;
//import com.cloudimpl.outstack.runtime.repo.StreamEvent;
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import com.github.benmanes.caffeine.cache.Expiry;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.BiFunction;
//import lombok.extern.slf4j.Slf4j;
//import reactor.core.publisher.Mono;
//
///**
// *
// * @author nuwan
// */
//@Slf4j
//public class RemoteCache {
//
//    private final Cache<String, Entity> cache;
//
//    private final RepoStreamingReq streamReq;
//    private final String domainOwner;
//    private final String domainContext;
//    private final StreamClient streamClient;
//    private final Cluster cluster;
//
//    public RemoteCache(Builder builder) {
//        this.streamReq = builder.streamReq;
//        this.domainOwner = builder.domainOwner;
//        this.domainContext = builder.domainContext;
//        this.cluster = builder.cluster;
//        this.streamClient = new StreamClient(cluster);
//        this.cache = Caffeine.newBuilder().expireAfter(new Expiry<String, Entity>() {
//            @Override
//            public long expireAfterCreate(String key, Entity value, long currentTime) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public long expireAfterUpdate(String key, Entity value, long currentTime, long currentDuration) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public long expireAfterRead(String key, Entity value, long currentTime, long currentDuration) {
//                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//            }
//        }).build();
//    }
//
//    public void init() {
//        this.streamClient.subscribeToMicroService(domainOwner, domainContext, streamReq).doOnNext(this::updateCache)
//                .doOnError(err -> log.error("error subscribing  to streaming {}:{}", this.domainOwner, this.domainContext))
//                .subscribe();
//    }
//
//    public void put(String id, Entity e) {
//        Entity old = cache.getIfPresent(e.id());
//        if (old == null) {
//            cache.put(e.id(), e);
//        } else if (old.getMeta().getLastSeq() < e.getMeta().getLastSeq()) {
//            cache.put(e.id(), e);
//        }
//    }
//
//    public Optional<Entity> 
//    private void updateCache(StreamEvent event) {
//        Entity e = (Entity) event.getEvent();
//        Entity old = cache.getIfPresent(e.id());
//        switch (event.getAction()) {
//            case ADD: {
//                if (old == null) {
//                    cache.put(e.id(), e);
//                } else if (old.getMeta().getLastSeq() < e.getMeta().getLastSeq()) {
//                    cache.put(e.id(), e);
//                }
//                break;
//            }
//            case REMOVE: {
//                if (old != null && old.getMeta().getLastSeq() == e.getMeta().getLastSeq()) {
//                    cache.invalidate(e.id());
//                }
//                break;
//            }
//        }
//    }
//
//    public static final class Builder {
//
//        private BiFunction<String, Object, Mono> requestHandler;
//        private RepoStreamingReq streamReq;
//        private String domainOwner;
//        private String domainContext;
//        private Cluster cluster;
//
//        public Builder withMicroService(String domainOwner, String domainContext) {
//            this.domainOwner = domainOwner;
//            this.domainContext = domainContext;
//            return this;
//        }
//
//        public Builder withStreamReq(RepoStreamingReq req) {
//            streamReq = req;
//            return this;
//        }
//
//        public Builder withInitialReq(BiFunction<String, Object, Mono> requestHandler) {
//            this.requestHandler = requestHandler;
//            return this;
//        }
//
//        public Builder withCluster(Cluster cluster) {
//            this.cluster = cluster;
//            return this;
//        }
//    }
//}
