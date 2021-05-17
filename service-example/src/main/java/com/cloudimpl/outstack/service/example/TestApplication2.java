package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityContext;
import com.cloudimpl.outstack.runtime.EntityContextProvider;
import com.cloudimpl.outstack.runtime.EventRepository;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;

import java.util.List;

public class TestApplication2 {

    public static void main(String[] args) {

        EntityCommandHandler<Organization, OrganizationCreateRequest, Organization> entityCommandHandler = new CreateOrganization();
        OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                .withOrgName("Test Organization")
                .withWebsite("http://localhost/testorg")
                .build();

        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);

        if (RootEntity.isMyType(entityCommandHandler.getEntityType())) {
            EventRepository<?> repository = eventRepositoryFactory.createRepository(entityCommandHandler.getEntityType());
            EntityContextProvider<?> contextProvider = new EntityContextProvider<>(repository::loadEntityWithClone,
                    repository::generateTid, repository, eventRepositoryFactory::createRepository);
            EntityContext<Organization> emit = entityCommandHandler.emit(contextProvider, organizationCreateRequest);
            List<Event> events = emit.getEvents();
            String entityTRN = emit.getEvents().get(0).getEntityTRN();
        } else {

        }

        executeCommand(organizationCreateRequest);
    }

    public static void executeCommand(Command command) {

//        System.out.println(command.commandName());
//
//        ResourceHelper resourceHelper = new ResourceHelper("cloudimpl", "test", "api");
//        EventRepositoryFactory eventRepositoryFactory = new MemEventRepositoryFactory(resourceHelper);
//        EventRepository<?> repository = eventRepositoryFactory.createRepository(entityCommandHandler.getEntityType());
//        EntityContextProvider<?> contextProvider = new EntityContextProvider<>(repository::loadEntityWithClone,
//                repository::generateTid, repository, eventRepositoryFactory::createRepository);
//        EntityContext<Organization> emit = entityCommandHandler.emit(contextProvider, organizationCreateRequest);

    }
}
