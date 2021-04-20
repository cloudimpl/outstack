/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.Event;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwansa
 */
public interface EventStream {
    void publish(Event event);
    void checkpoint();
    Flux<Event> flux();
}
