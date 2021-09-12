/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.spring.component.swagger;

import com.cloudimpl.outstack.common.Pair;
import com.cloudimpl.outstack.core.Inject;
import com.cloudimpl.outstack.runtime.EntityQueryContext;
import com.cloudimpl.outstack.runtime.EntityQueryHandler;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.runtime.domainspec.QueryByIdRequest;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorContextManager;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorManager;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorVersionManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sound.midi.Patch;

/**
 *
 * @author nuwan
 */
public class ListApiDoc extends EntityQueryHandler<ApiDoc, QueryByIdRequest, SwaggerResource> {

    @Inject
    private ServiceDescriptorContextManager manager;

    private List<ResourceTemplates.Path> paths = new LinkedList<>();

    @Inject
    private ResourceHelper helper;
    @Override
    protected SwaggerResource execute(EntityQueryContext<ApiDoc> context, QueryByIdRequest query) {
        SwaggerResource res = new SwaggerResource();
        res.setBasePath("/");
        res.setInfo(ResourceTemplates.Info.builder().description("swagger documentation  for no-code rest point in outstack microservice engine")
                .title("Swagger By CloudImpl COM")
                .version("1.0")
                .contact(ResourceTemplates.Contact.builder().email("info@cloudimpl.com").name("CloudImpl COM").url("http://www.cloudimpl.com").build())
                .license(ResourceTemplates.License.builder().name("Apache License Version 2.0").build()).build());
        res.setSwagger("2.0");
        ServiceDescriptorVersionManager versionManager = manager.getByContext(helper.getApiContext());
        res.setTags(getTags(versionManager));
        res.setPaths(extractPaths(versionManager));
        return res;
    }

    private Map<String,Object> extractPaths(ServiceDescriptorVersionManager vMan) {
         
         return vMan.getCmdDescriptors().stream().flatMap(m->extractPathByVersionForPost(m.getKey(), m.getValue()).stream()).collect(Collectors.toMap(p->p.getPath(), p->p.getItems()));
    }
    
    
    private List<ResourceTemplates.Tag> getTags(ServiceDescriptorVersionManager vMan)
    {
        Set<String> set= new HashSet<>();
        set.addAll(vMan.getCmdDescriptors().stream().flatMap(d->d.getValue().getDescriptors().stream()).map(d->d.getPlural()).collect(Collectors.toList()));
        return set.stream().map(s->ResourceTemplates.Tag.builder().name(s).description(s).build()).collect(Collectors.toList());
    }
    
    private Collection<ResourceTemplates.Path> extractPathByVersionForPost(String version,ServiceDescriptorManager man)
    {
                   
        List<Map<String ,ResourceTemplates.Path>> list = man.getDescriptors().stream()
                .map(d->extractPathFromCommandDescriptor(version, d))
                .collect(Collectors.toList());
         return list.stream().flatMap(m->m.values().stream()).collect(Collectors.toList());
    }
    
    private Map<String ,ResourceTemplates.Path> extractPathFromCommandDescriptor(String version,SpringServiceDescriptor desc)
    {
        Map<String,ResourceTemplates.Path> pathMap = new HashMap<>();
        ResourceTemplates.Path path = new ResourceTemplates.Path().withType("post",ResourceTemplates.Post.builder()
                .operationId("create"+desc.getPlural()+"UsingPOST").produces(Collections.singletonList("application/json")).summary(desc.getPlural()).tags(Collections.singletonList(desc.getPlural())).build());
        List<ResourceTemplates.Parameter> params = new LinkedList<>();
        
        ResourceTemplates.Parameter param = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
//        ResourceTemplates.Item item = new ResourceTemplates.Item();
//        item.setType("string");
//        item.setDefault("Create"+desc.getRootType());
        desc.getRootActions().stream().forEach(action->param.addEnum(action.getName()));
       // param.setItems(item);
        params.add(param);
        
        ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get("post");
        post.setParameters(params);
        path.withPath("/"+version+"/"+desc.getPlural());
        pathMap.put(path.getPath(), path);
        return pathMap;
    }
}
