/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.CloudServiceDescriptor;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.core.Named;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.core.logger.ILogger;
import com.cloudimpl.outstack.runtime.EntityIdHelper;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.runtime.domain.CommandHandlerRegistered;
import com.cloudimpl.outstack.runtime.domain.DomainContext;
import com.cloudimpl.outstack.runtime.domain.DomainContextCreated;
import com.cloudimpl.outstack.runtime.domain.EventHandlerRegistered;
import com.cloudimpl.outstack.runtime.domain.ServiceModule;
import com.cloudimpl.outstack.runtime.domain.ServiceModuleProvisioned;
import com.cloudimpl.outstack.runtime.domain.QueryHandlerRegistered;
import com.cloudimpl.outstack.runtime.domain.ServiceModuleRef;
import com.cloudimpl.outstack.runtime.domain.ServiceModuleRefCreated;
import com.cloudimpl.outstack.spring.util.SpringUtil;
import java.util.Collection;
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
public class RestControllerService implements Function<CloudMessage, CloudMessage> {

    private Flux<FluxMap.Event<String, CloudService>> serviceFlux;
    private ServiceDescriptorContextManager serviceManager;
    private ServiceDescriptorVersionManager serviceQieryManager;
    private ILogger logger;
    private EventRepositoryFactory eventRepoFactory;
    private Map<String,DomainContext> domainContexts = new ConcurrentHashMap<>();
    
    @Inject
    public RestControllerService(@Named("@serviceFlux") Flux<FluxMap.Event<String, CloudService>> serviceFlux,
            ServiceDescriptorContextManager serviceManager,@Named("MemRepositoryFactory") EventRepositoryFactory eventRepoFactory, ILogger logger) {
        this.logger = logger.createSubLogger(RestControllerService.class);
        this.eventRepoFactory = eventRepoFactory;
        this.serviceFlux = serviceFlux;
        this.serviceManager = serviceManager;
        this.serviceFlux.filter(s -> s.getType() == FluxMap.Event.Type.ADD)
                .map(s -> getSpringDescriptor(s.getValue().getDescriptor()))
                .filter(d -> d.isPresent())
                .map(d -> d.get())
                .doOnNext(this::addCmdDescriptor)
                .doOnError(err -> logger.exception(err, "rest controller service error:"))
                .subscribe();

        this.serviceFlux.filter(s -> s.getType() == FluxMap.Event.Type.ADD)
                .map(s -> getSpringQueryDescriptor(s.getValue().getDescriptor()))
                .filter(d -> d.isPresent())
                .map(d -> d.get())
                .doOnNext(this::addQueryDescriptor)
                .doOnError(err -> logger.exception(err, "rest controller service error:"))
                .subscribe();
    }

    private Optional<SpringServiceDescriptor> getSpringDescriptor(CloudServiceDescriptor serviceDescriptor) {
        String str = serviceDescriptor.getAttr().get("serviceMeta");
        if (str != null) {
            SpringServiceDescriptor serviceDesc = GsonCodec.decode(SpringServiceDescriptor.class, str);
            return Optional.of(serviceDesc);
        }
        return Optional.empty();
    }

    private Optional<SpringServiceDescriptor> getSpringQueryDescriptor(CloudServiceDescriptor serviceDescriptor) {
        String str = serviceDescriptor.getAttr().get("serviceQueryMeta");
        if (str != null) {
            SpringServiceDescriptor serviceDesc = GsonCodec.decode(SpringServiceDescriptor.class, str);
            return Optional.of(serviceDesc);
        }
        return Optional.empty();
    }

    private void addCmdDescriptor(SpringServiceDescriptor desc) {
        this.serviceManager.putCmdContext(desc.getApiContext(), desc.getVersion(), desc);
        addEntities(desc);
    }

    private void addQueryDescriptor(SpringServiceDescriptor desc) {
        this.serviceManager.putQueryContext(desc.getApiContext(), desc.getVersion(), desc);
        addEntities(desc);
    }

    @Override
    public CloudMessage apply(CloudMessage req) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addEntities(SpringServiceDescriptor desc) {

        ServiceModule service = eventRepoFactory.createOrGetRepository(ServiceModule.class).getRootById(ServiceModule.class, desc.getRootType(), null).orElseGet(()->createMicroService(desc));
        addActions(eventRepoFactory.createOrGetRepository(ServiceModule.class),service.id(), desc.getRootType(), desc, desc.getRootActions());
        desc.entityDescriptors().forEach(ed -> {
            addActions(eventRepoFactory.createOrGetRepository(ServiceModule.class),service.id(), ed.getName(), desc, desc.getChildActions(ed.getName()));
        });
    }

    private ServiceModule createMicroService(SpringServiceDescriptor desc) {
        DomainContext domainContext = createOrGetDomainContext(desc);
        
        String id = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(desc.getDomainOwner()+"/"+desc.getDomainContext() + ":" + desc.getRootType());
        ServiceModuleProvisioned provisioned = new ServiceModuleProvisioned(desc.getServiceName(), desc.getRootType(), desc.getVersion(), desc.getApiContext(), desc.getTenancy());
        provisioned.setId(id);
        provisioned.setRootId(id);
        provisioned.setAction(Event.Action.CREATE);
        EntityHelper.setVersion(provisioned, Entity.getVersion(ServiceModule.class));
        EntityHelper.setCreatedDate(provisioned, System.currentTimeMillis());
        
        String childId = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(desc.getDomainOwner()+"/"+desc.getDomainContext() + ":" + id);
        ServiceModuleRefCreated refEvent = new ServiceModuleRefCreated(EntityIdHelper.idToRefId(id), domainContext.entityId());
        refEvent.setId(childId);
        refEvent.setRootId(domainContext.id());
        refEvent.setAction(Event.Action.CREATE);
        EntityHelper.setVersion(refEvent, Entity.getVersion(ServiceModuleRef.class));
        EntityHelper.setCreatedDate(refEvent, System.currentTimeMillis());
        eventRepoFactory.createOrGetRepository(DomainContext.class).applyEvent(refEvent);
        return eventRepoFactory.createOrGetRepository(ServiceModule.class).applyEvent(provisioned);
    }

    private void addActions(EventRepositoy eventRepo,String rootId, String targetEntity, SpringServiceDescriptor serviceDesc, Collection<SpringServiceDescriptor.ActionDescriptor> actions) {
        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER).forEach(action -> {
            CommandHandlerRegistered event = new CommandHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(serviceDesc.getDomainOwner()+"/"+serviceDesc.getDomainContext() + ":" + serviceDesc.getRootType() + ":cmd:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setVersion(event, Entity.getVersion(ServiceModule.class));
            EntityHelper.setCreatedDate(event, System.currentTimeMillis());
            eventRepo.applyEvent(event);
        });

        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.EVENT_HANDLER).forEach(action -> {
            EventHandlerRegistered event = new EventHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(serviceDesc.getDomainOwner()+"/"+serviceDesc.getDomainContext() + ":" + serviceDesc.getRootType() + ":evt:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setVersion(event, Entity.getVersion(ServiceModule.class));
            EntityHelper.setCreatedDate(event, System.currentTimeMillis());
            eventRepo.applyEvent(event);
        });

        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER).forEach(action -> {
            QueryHandlerRegistered event = new QueryHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(serviceDesc.getDomainOwner()+"/"+serviceDesc.getDomainContext() + ":" + serviceDesc.getRootType() + ":query:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setCreatedDate(event, System.currentTimeMillis());
             EntityHelper.setVersion(event, Entity.getVersion(ServiceModule.class));
            eventRepo.applyEvent(event);
        });
    }
    
    private DomainContext createOrGetDomainContext(SpringServiceDescriptor serviceDesc)
    {
        return domainContexts.computeIfAbsent(serviceDesc.getDomainOwner()+"/"+serviceDesc.getDomainContext(), id->{
            String tid = EventRepositoy.TID_PREFIX + "i" + SpringUtil.toMD5(serviceDesc.getDomainOwner()+"/"+serviceDesc.getDomainContext() + ":" + id);
            DomainContextCreated event = new DomainContextCreated(id, serviceDesc.getDomainOwner(), serviceDesc.getDomainContext());
            event.setId(tid);
            event.setRootId(tid);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setVersion(event, Entity.getVersion(DomainContext.class));
            EntityHelper.setCreatedDate(event, System.currentTimeMillis());
            return eventRepoFactory.createOrGetRepository(DomainContext.class).applyEvent(event);
        });
    }
}
