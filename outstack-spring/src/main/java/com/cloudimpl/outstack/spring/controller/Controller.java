/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.NotImplementedException;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorManager;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.web.bind.annotation.RestController;
/**
 *
 * @author nuwan
 */
@RestController
@RequestMapping("/")
public class Controller{

    @Autowired
    Cluster cluster;
    
    @PostMapping(value = "api/{version}/{rootEntity}",consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> createRootEntity(@PathVariable String version,@PathVariable String rootEntity,@RequestBody String body) {
        
        SpringServiceDescriptor serviceDesc = getServiceDescriptor(version, rootEntity);
        String rootType = serviceDesc.getRootType();
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction("Create"+rootType).orElseThrow(()->new NotImplementedException("resource  {0} creation not implemented",rootType));
        CommandWrapper request = new CommandWrapper(action.getName(),body);
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }
    
    @GetMapping("/stream")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i->"tick"+i+"\n");
    }
    
    
    private ServiceDescriptorManager getSrvDescByVersion(String version)
    {
        return cluster.getServiceDescriptorManager().getVersion(version).orElseThrow(()->new ResourceNotFoundException("api version {0} not found",version));
    }
    
    private SpringServiceDescriptor getServiceDescriptor(ServiceDescriptorManager man,String rootTypePlural)
    {
        return man.getServiceDescriptorByPlural(rootTypePlural).orElseThrow(()->new ResourceNotFoundException("resource {0} not found",rootTypePlural));
    }
    
    
    private SpringServiceDescriptor getServiceDescriptor(String version,String rootTypePlural)
    {
        return getServiceDescriptor(getSrvDescByVersion(version),rootTypePlural);
    }
    
}
