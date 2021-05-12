/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.domain;

import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
@EntityMeta(plural = "MicroServices",version = "v1")
public class MicroService extends RootEntity {

    private final String rootEntity;
    private  String serviceName;
    private String version;
    private boolean tenantService;
    private String apiContext;

    public MicroService(String rootEntity) {
        this.rootEntity = rootEntity;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isTenantService() {
        return this.tenantService;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getRootEntity() {
        return rootEntity;
    }

    @Override
    public String entityId() {
        return rootEntity;
    }

    private void applyEvent(MicroServiceProvisioned microServiceProvisioned) {
        this.serviceName = microServiceProvisioned.getServiceName();
        this.version = microServiceProvisioned.getVersion();
        this.tenantService = microServiceProvisioned.isTenantService();
        this.apiContext = microServiceProvisioned.getApiContext();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "MicroServiceProvisioned": {
                applyEvent((MicroServiceProvisioned) event);
                break;
            }
            default: {
                throw new DomainEventException(DomainEventException.ErrorCode.UNHANDLED_EVENT, "unhandled event:" + event.getClass().getName());
            }
        }
    }

    @Override
    public String idField() {
        return "rootEntity";
    }

}
