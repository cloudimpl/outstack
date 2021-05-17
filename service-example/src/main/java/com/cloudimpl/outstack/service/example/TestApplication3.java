package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.Tenant;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.domain.example.commands.TenantCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepository;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;

import java.util.List;

public class TestApplication3 {

    public static void main(String[] args) {
        EntityCommandHandler<Tenant, TenantCreateRequest, Tenant> entityCommandHandler = new CreateTenant();
        OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                .withOrgName("Test Organization")
                .withWebsite("http://localhost/testorg")
                .build();
        TenantCreateRequest tenantCreateRequest = TenantCreateRequest.builder()
                .withOrgName("Test Organization")
                .withTenantName("Test Tenant")
                .withEndpoint("http://localhost:8080/")
                .withRootId("id-test")
                .build();

        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);

        if (!RootEntity.isMyType(entityCommandHandler.getEntityType())) {
            EventRepository<?> repository = eventRepositoryFactory.createRepository(Organization.class);
            EntityContextProvider<?> contextProvider = new EntityContextProvider<>(repository::loadEntityWithClone,
                    repository::generateTid, repository, eventRepositoryFactory::createRepository);
            EntityContext<Tenant> emit = entityCommandHandler.emit(contextProvider, organizationCreateRequest);
            List<Event> events = emit.getEvents();
            String entityTRN = emit.getEvents().get(0).getEntityTRN();
        }
    }
}