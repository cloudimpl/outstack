/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.CloudServiceDescriptor;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.core.logger.ILogger;
import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import reactor.core.publisher.Flux;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "RestController")
@Router(routerType = RouterType.LOCAL)
public class RestControllerService implements Function<CloudMessage, CloudMessage>{

    private Flux<FluxMap.Event<String, CloudService>> serviceFlux;
    private ServiceDescriptorVersionManager serviceManager;
    private ILogger logger;
    @Inject
    public RestControllerService( @Named("@serviceFlux")Flux<FluxMap.Event<String, CloudService>> serviceFlux,ServiceDescriptorVersionManager serviceManager,ILogger logger) {
        this.logger = logger.createSubLogger(RestControllerService.class);
        this.serviceFlux = serviceFlux;
        this.serviceManager = serviceManager;
        this.serviceFlux.filter(s->s.getType() == FluxMap.Event.Type.ADD)
                .map(s->getSpringDescriptor(s.getValue().getDescriptor()))
                .filter(d->d.isPresent())
                .map(d->d.get())
                .doOnNext(this::addDescriptor)
                .doOnError(err->logger.exception(err, "rest controller service error:"))
                .subscribe();
    }
    
    private Optional<SpringServiceDescriptor> getSpringDescriptor(CloudServiceDescriptor serviceDescriptor)
    {
        String str = serviceDescriptor.getAttr().get("seviceMeta");
        if(str != null)
        {
            SpringServiceDescriptor serviceDesc = GsonCodec.decode(SpringServiceDescriptor.class, str);
            return Optional.of(serviceDesc);
        }
        return Optional.empty();
    }
    
    private void addDescriptor(SpringServiceDescriptor desc)
    {
        this.serviceManager.put(desc);
    }
    
    @Override
    public CloudMessage apply(CloudMessage req) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
