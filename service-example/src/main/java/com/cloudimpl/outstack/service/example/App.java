/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service.example;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.EventRepositoy;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.repo.MemEventRepository;
import com.xventure.projectA.OrganizationCreated;
import com.xventure.projectA.user.User;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 */
public class App {
    public static void main(String[] args) {
        EventRepositoy<User> repo = new MemEventRepository<>(User.class,null);
        ResourceHelper helper = new ResourceHelper("test", "abc");
        UserService service = new UserService(repo, helper);
        Mono.from(service.apply(new UserCreateReq())).doOnError(err->Throwable.class.cast(err).printStackTrace())
                .doOnNext(e->System.out.println(GsonCodec.encode(e)))
                .flatMap(e->service.apply(new OrgCreateRequest().withRootTid(((User)e).tid())))
                .doOnError(err->Throwable.class.cast(err).printStackTrace())
                .doOnNext(e->System.out.println(GsonCodec.encode(e)))
                .subscribe(); 
  //      Mono.from(service.apply(new UserCreateReq())).doOnError(err->Throwable.class.cast(err).printStackTrace()).subscribe();
        //  Mono.from(service.apply(new OrgCreateRequest())).doOnError(err->Throwable.class.cast(err).printStackTrace()).subscribe();
    }
}
