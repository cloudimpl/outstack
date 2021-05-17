package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepository;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.RootEntityContext;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TestApplication {

    public static void main(String[] args) {

        EntityCommandHandler<Organization, OrganizationCreateRequest, Organization> entityCommandHandler = new CreateOrganization();
        OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                .withOrgName("Test Organization")
                .withWebsite("http://localhost/testorg")
                .build();

        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);

        if (RootEntity.isMyType(entityCommandHandler.getEntityType())) {

            EventRepository<Organization> repository = eventRepositoryFactory.createRepository(Organization.class);
            EntityContextProvider<Organization> contextProvider = new EntityContextProvider<>(repository::loadEntityWithClone,
                    repository::generateTid, repository, eventRepositoryFactory::createRepository);
            EntityContext<Organization> emit = entityCommandHandler.emit(contextProvider, organizationCreateRequest);

            // Expose emit transaction through helper function

            // Output - Events, Entities, Exceptions

            List<Event> events = emit.getEvents();
            String entityTRN = emit.getEvents().get(0).getEntityTRN();

            Optional<Entity> entity = contextProvider.createWritableTransaction(entityTRN, null).loadEntity(Organization.class, entityTRN, null, null, null);

            Optional<Organization> rootById = repository.getRootById(Organization.class, entityTRN, null);
            Query.PagingRequest pagingRequest = new Query.PagingRequest(0, 100, Collections.emptyList());
            Collection<Organization> allByRootType = repository.getAllByRootType(Organization.class, null, pagingRequest);
            for (Organization organization: allByRootType) {
                System.out.println(organization.getOrgName());
            }
        } else {

        }
    }
}
