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
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.EventRepository;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityHelper;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.domain.CommandHandlerRegistered;
import com.cloudimpl.outstack.spring.domain.EventHandlerRegistered;
import com.cloudimpl.outstack.spring.domain.MicroService;
import com.cloudimpl.outstack.spring.domain.MicroServiceProvisioned;
import com.cloudimpl.outstack.spring.domain.QueryHandlerRegistered;
import com.cloudimpl.outstack.spring.util.SpringUtil;
import java.util.Collection;
import java.util.Optional;
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
    private ResourceHelper resourceHelper;
    private ILogger logger;
    private EventRepository<MicroService> eventRepo;

    @Inject
    public RestControllerService(@Named("@serviceFlux") Flux<FluxMap.Event<String, CloudService>> serviceFlux,
            ResourceHelper resourceHelper,
            ServiceDescriptorContextManager serviceManager, EventRepositoryFactory eventRepoFactory, ILogger logger) {
        this.logger = logger.createSubLogger(RestControllerService.class);
        this.eventRepo = eventRepoFactory.createRepository(MicroService.class);
        this.resourceHelper = resourceHelper;
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

        MicroService service = eventRepo.getRootById(MicroService.class, desc.getRootType(), null).orElse(createMicroService(desc));
        addActions(service.id(), desc.getRootType(), desc, desc.getRootActions());
        desc.entityDescriptors().forEach(ed -> {
            addActions(service.id(), ed.getName(), desc, desc.getChildActions(ed.getName()));
        });
    }

    private MicroService createMicroService(SpringServiceDescriptor desc) {
        String id = EventRepository.TID_PREFIX + "i" + SpringUtil.toMD5(resourceHelper + ":" + desc.getRootType());
        MicroServiceProvisioned provisioned = new MicroServiceProvisioned(desc.getServiceName(), desc.getRootType(), desc.getVersion(), desc.getApiContext(), desc.isTenantService());
        provisioned.setId(id);
        provisioned.setRootId(id);
        provisioned.setAction(Event.Action.CREATE);
         EntityHelper.setVersion(provisioned, Entity.getVersion(MicroService.class));
        return eventRepo.applyEvent(provisioned);
    }

    private void addActions(String rootId, String targetEntity, SpringServiceDescriptor serviceDesc, Collection<SpringServiceDescriptor.ActionDescriptor> actions) {
        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER).forEach(action -> {
            CommandHandlerRegistered event = new CommandHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepository.TID_PREFIX + "i" + SpringUtil.toMD5(resourceHelper + ":" + serviceDesc.getRootType() + ":cmd:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setVersion(event, Entity.getVersion(MicroService.class));
            eventRepo.applyEvent(event);
        });

        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.EVENT_HANDLER).forEach(action -> {
            EventHandlerRegistered event = new EventHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepository.TID_PREFIX + "i" + SpringUtil.toMD5(resourceHelper + ":" + serviceDesc.getRootType() + ":evt:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
            EntityHelper.setVersion(event, Entity.getVersion(MicroService.class));
            eventRepo.applyEvent(event);
        });

        actions.stream().filter(action -> action.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER).forEach(action -> {
            QueryHandlerRegistered event = new QueryHandlerRegistered(action.getName(), targetEntity, serviceDesc.getRootType());
            String childId = EventRepository.TID_PREFIX + "i" + SpringUtil.toMD5(resourceHelper + ":" + serviceDesc.getRootType() + ":query:" + action.getName());
            event.setId(childId);
            event.setRootId(rootId);
            event.setAction(Event.Action.CREATE);
             EntityHelper.setVersion(event, Entity.getVersion(MicroService.class));
            eventRepo.applyEvent(event);
        });
    }
}
