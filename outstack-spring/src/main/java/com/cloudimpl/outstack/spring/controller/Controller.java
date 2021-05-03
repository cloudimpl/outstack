/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.spring.component.Cluster;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/")
public class Controller{

    @Autowired
    Cluster cluster;
    
    @GetMapping("{id}")
    private Mono<String> getChildEntity(@PathVariable String id) {
        return Mono.just("reply:"+id + " cluster"+cluster.toString());
    }
    
    @GetMapping("/stream")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i->"tick"+i+"\n");
    }
}
