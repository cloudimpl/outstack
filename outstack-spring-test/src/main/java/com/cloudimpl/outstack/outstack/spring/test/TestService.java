//package com.cloudimpl.outstack.outstack.spring.test;
//
//import com.cloudimpl.outstack.domain.example.Organization;
//import com.cloudimpl.outstack.domain.example.Tenant;
//import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
//import com.cloudimpl.outstack.runtime.EntityCommandHandler;
//import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
//import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
//import com.cloudimpl.outstack.spring.component.SpringService;
//
//import java.util.List;
//
//public class TestService<T extends RootEntity> extends SpringService<T> {
//
//    static {
//        $(CreateOrganization.class);
//        $(UpdateOrganization.class);
//        $(CreateTenant.class);
//    }
//    public TestService(Class<T> rootEntityType, EventRepositoryFactory factory, List<EntityCommandHandler<?, ? ,?>> entityCommandHandlers) {
//        super(factory);
//        entityCommandHandlers.stream().forEach(e -> {
//            $(e);
//        });
//        $(CreateOrganization.class);
//        $(UpdateOrganization.class);
//        $(CreateTenant.class);
//        $$(Organization.class);
//        $$(Tenant.class); // auto bind
//    }
//}
