/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.repo.MemEventRepository;
import com.restrata.platform.Organization;
import com.restrata.platform.commands.OrganizationCreateRequest;

import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class App {
    public static void main(String[] args) {
        EventRepositoy<Organization> repo = new MemEventRepository<>(Organization.class,null);
        ResourceHelper helper = new ResourceHelper("test", "abc");
        OrganizationService service = new OrganizationService(repo, helper);
        Mono.from(service.apply(CloudMessage.builder().withData(OrganizationCreateRequest.builder().withOrgName("testOrg").withCommand("createOrganization").build()).build())).doOnError(err->Throwable.class.cast(err).printStackTrace())
                .doOnNext(e->System.out.println(GsonCodec.encode(e)))
                //.flatMap(e->service.apply(CloudMessage.builder().withData(new OrgCreateRequest(((User)e).id())).build()))
                .doOnError(err->Throwable.class.cast(err).printStackTrace())
                .doOnNext(e->System.out.println(GsonCodec.encode(e)))
               // .flatMap(e->service.apply(CloudMessage.builder().withData(new OrgCreateRequest(((Organization)e).rootId())).build()))
                .subscribe(); 
  //      Mono.from(service.apply(new UserCreateReq())).doOnError(err->Throwable.class.cast(err).printStackTrace()).subscribe();
        //  Mono.from(service.apply(new OrgCreateRequest())).doOnError(err->Throwable.class.cast(err).printStackTrace()).subscribe();
    }
}
