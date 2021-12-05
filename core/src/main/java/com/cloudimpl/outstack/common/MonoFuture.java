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
package com.cloudimpl.outstack.common;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class MonoFuture<T> {

    private final Mono<T> mono;
    private final CompletableFuture<T> future;
    private final List<Object> params;
    private MonoFuture(Object[] params) {
        this.params = Arrays.asList(params).stream().collect(Collectors.toList());
        this.future = new CompletableFuture<>();
        this.mono = Mono.fromFuture(future);
    }

    public void submit(T t) {
        future.complete(t);
    }

    public Mono<T> get() {
        return mono;
    }

    public <P> P getParam(int index)
    {
        return (P)this.params.get(index);
    }
    
    public static <T> MonoFuture<T> create(Object... args) {
        return new MonoFuture<>(args);
    }
}
