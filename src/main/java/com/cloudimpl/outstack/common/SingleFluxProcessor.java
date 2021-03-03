/*
 * Copyright 2021 nuwansa.
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

import java.util.function.Consumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 *
 * @author nuwansa
 */
public class SingleFluxProcessor<T> {

    private FluxSink<T> fluxSink;
    private final Flux<T> flux;
    public SingleFluxProcessor(Runnable sinkEmitter) {
        flux = Flux.create(emitter->this.setEmitter(emitter,sinkEmitter));
    }

    private void setEmitter(FluxSink<T> emitter,Runnable sinkEmitter) {
        if (fluxSink != null) {
            emitter.error(new IllegalStateException("only allowed once subscription"));
            return;
        }
        this.fluxSink = emitter;
        this.fluxSink.onCancel(() -> this.fluxSink = null);
        this.fluxSink.onDispose(() -> this.fluxSink = null);
        sinkEmitter.run();
    }

    public void send(T item) {
        if (fluxSink != null) {
            fluxSink.next(item);
        }
    }

    public void complete() {
        if (fluxSink != null) {
            fluxSink.complete();
        }
    }

    public void error(Throwable thr) {
        if (fluxSink != null) {
            fluxSink.error(thr);
        }
    }
    
    public Flux<T> flux()
    {
        return flux;
    }
}
