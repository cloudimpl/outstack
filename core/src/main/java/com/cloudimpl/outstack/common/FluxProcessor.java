/*
 * Copyright 2020 nuwansa.
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 *
 * @author nuwansa
 * @param <T>
 */
public class FluxProcessor<T> {

    private final List<XFluxSink<T>> list;
    private final Flux<T> flux;

    public FluxProcessor(String name) {
        this(name, t -> {
        });
    }

    public FluxProcessor(String name, Consumer<FluxSink<T>> consumer) {
        list = new CopyOnWriteArrayList<>();
        flux = Flux.<T>create(emitter -> {
            System.out.println("subscription added:" + Thread.currentThread().getName() + "subscriber : " + name + ":" + emitter.currentContext().get("subscriber"));
            consumer.accept(emitter);
            XFluxSink s = new XFluxSink<>(emitter.currentContext().get("subscriber"), emitter);
            list.add(s);
            emitter.onCancel(() -> this.remove(s));
            emitter.onDispose(() -> this.remove(s));
        });
    }

    public void add(T t) {
        list.forEach(sink -> {
            try {
                sink.next(t);
            } catch (Throwable ex) {
                System.out.println("xxxxxxxx: " + ex.getMessage());
            }

        });
    }

    public Flux<T> flux(String subscriber) {
        return flux.contextWrite(ctx -> ctx.put("subscriber", subscriber));
    }

    private void remove(XFluxSink sink) {
        System.out.println("remove from :" + Thread.currentThread().getName());
        list.remove(sink);
    }

    public static final class XFluxSink<T> {

        private String name;
        private FluxSink<T> sink;

        public XFluxSink(String name, FluxSink<T> sink) {
            this.name = name;
            this.sink = sink;
        }

        public String getName() {
            return name;
        }

        public FluxSink<T> getSink() {
            return sink;
        }

        public XFluxSink<T> next(T msg) {
          //  System.out.println("send to: " + name + ":" + msg);
            this.sink.next(msg);
            return this;
        }

    }

}
