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
@EntityMeta(plural = "ServiceModules",version = "v1")
public class ServiceModule extends RootEntity {

    private final String rootEntity;
    private  String serviceName;
    private String version;
    private boolean tenantService;
    private String apiContext;

    public ServiceModule(String rootEntity) {
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

    private void applyEvent(ServiceModuleProvisioned serviceModuleProvisioned) {
        this.serviceName = serviceModuleProvisioned.getServiceName();
        this.version = serviceModuleProvisioned.getVersion();
        this.tenantService = serviceModuleProvisioned.isTenantService();
        this.apiContext = serviceModuleProvisioned.getApiContext();
    }

    @Override
    protected void apply(Event event) {
        switch (event.getClass().getSimpleName()) {
            case "ServiceModuleProvisioned": {
                applyEvent((ServiceModuleProvisioned) event);
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
