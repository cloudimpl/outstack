/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.coreImpl;

import com.cloudimpl.outstack.common.FluxMap;
import com.cloudimpl.outstack.core.CloudService;
import com.cloudimpl.outstack.core.logger.ILogger;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 *
 * @author nuwansa
 */
public class CloudServiceRegistry {

    private final FluxMap<String, CloudService> services;
    private final FluxMap<String, LocalCloudService> localServices;
    private final ILogger logger;
    private final Scheduler schedular;

    public CloudServiceRegistry(ILogger logger) {
        this.schedular = Schedulers.newSingle("serviceRegistry");
        this.services = new FluxMap<>(this.schedular);
        this.localServices = new FluxMap<>(this.schedular);
        this.logger = logger.createSubLogger(CloudServiceRegistry.class);
    }

    public void register(CloudService service) {
        services.putIfAbsent(service.id(), service).doOnNext(s -> {
            this.logger.error("duplicate service id {0}, old = {1} , new = {2}", s.id(), s, service);
        }).switchIfEmpty(Mono.just(service).doOnNext(s -> initService(s))).subscribe();
    }

    private void initService(CloudService service) {
        try {
            service.init();
        } catch (Exception ex) {
            logger.exception(ex, "error registering service {0}", service.getDescriptor());
            services.remove(service.id());
        }
        if (service instanceof LocalCloudService) {
            localServices.put(service.id(), (LocalCloudService) service);
        }
    }

    public void unregister(String id) {
        services.remove(id)
                .doOnNext(s -> {
                    logger.info("service unregister id = {0} -> {1}", id, s);
                    if (s instanceof LocalCloudService) {
                        localServices.remove(id);
                    }
                })
                .subscribe();
    }

    public void unregisterByMemberId(String memberId) {
        logger.info("unregister by memberId {0}", memberId);
        services().filter(srv -> srv.memberId().equals(memberId)).forEach(srv -> unregister(srv.id()));
    }

    public Flux<FluxMap.Event<String, CloudService>> flux() {
        return services.flux();
    }

    public Flux<FluxMap.Event<String, LocalCloudService>> localFlux() {
        return localServices.flux();
    }

    public Stream<CloudService> services() {
        return services.values().stream();
    }

    public CloudService findLocalByName(String name) {
        return localServices.values().stream().filter(s -> s.name().equals(name)).findFirst().orElse(null);
    }

    public CloudService findLocal(String id) {
        CloudService service = localServices.get(id);
        if (service == null) {
            throw new ServiceRegistryException("local service with id " + id + " not found");
        }
        return service;
    }

    public CloudService findService(String id) {
        CloudService service = services.get(id);
        if (service == null) {
            throw new ServiceRegistryException("service with id " + id + " not found");
        }
        return service;
    }

    public boolean isServiceExist(String id) {
        return services.get(id) != null;
    }
}
