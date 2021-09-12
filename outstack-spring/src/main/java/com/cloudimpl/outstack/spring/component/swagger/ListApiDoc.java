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
import com.cloudimpl.outstack.runtime.domainspec.EnablePublicAccess;
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
@EnablePublicAccess
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

    private Map<String, Object> extractPaths(ServiceDescriptorVersionManager vMan) {

        List<ResourceTemplates.Path> cmdPaths = vMan.getCmdDescriptors().stream().flatMap(m -> extractPathByVersionForPost(m.getKey(), m.getValue()).stream()).collect(Collectors.toList());
        List<ResourceTemplates.Path> queryPaths = vMan.getQueryDescriptors().stream().flatMap(m -> extractPathByVersionForGet(m.getKey(), m.getValue()).stream()).collect(Collectors.toList());

        List<ResourceTemplates.Path> allPaths = new LinkedList<>();
        allPaths.addAll(cmdPaths);
        allPaths.addAll(queryPaths);

        return allPaths.stream().collect(Collectors.groupingBy(p -> p.getPath(), Collectors.mapping(p -> p, Collectors.toList()))).entrySet().stream().map(e -> ResourceTemplates.Path.merge(e.getKey(), e.getValue())).collect(Collectors.toMap(p -> p.getPath(), p -> p.getItems()));
    }

    private List<ResourceTemplates.Tag> getTags(ServiceDescriptorVersionManager vMan) {
        Set<String> set = new HashSet<>();
        set.addAll(vMan.getCmdDescriptors().stream().flatMap(d -> d.getValue().getDescriptors().stream()).map(d -> d.getPlural()).collect(Collectors.toList()));
        set.addAll(vMan.getQueryDescriptors().stream().flatMap(d -> d.getValue().getDescriptors().stream()).map(d -> d.getPlural()).collect(Collectors.toList()));
        return set.stream().map(s -> ResourceTemplates.Tag.builder().name(s).description(s).build()).collect(Collectors.toList());
    }

    private Collection<ResourceTemplates.Path> extractPathByVersionForPost(String version, ServiceDescriptorManager man) {

        return man.getDescriptors().stream()
                .flatMap(d -> extractPathFromCommandDescriptor(version, d).stream())
                .collect(Collectors.toList());
    }

    private Collection<ResourceTemplates.Path> extractPathByVersionForGet(String version, ServiceDescriptorManager man) {

        return man.getDescriptors().stream()
                .flatMap(d -> extractPathFromQueryDescriptor(version, d).stream())
                .collect(Collectors.toList());
    }

    private List<ResourceTemplates.Path> extractPathFromCommandDescriptor(String version, SpringServiceDescriptor desc) {
        List<ResourceTemplates.Path> rootPaths = desc.getRootActions().stream().filter(a->a.getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER)
                .map(a -> createRootPathForCommand(version, desc.getRootType(), desc.getPlural(), a)).collect(Collectors.toList());
        List<ResourceTemplates.Path> childPaths = desc.getChildActions().stream().filter(a->a.getValue().getActionType() == SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER)
                .map(p -> createChildPathForCommand(version, desc.getRootType(), desc.getPlural(), p.getKey(), desc.getEntityDescByName(p.getKey()).getPlural(), p.getValue())).collect(Collectors.toList());
        LinkedList<ResourceTemplates.Path> paths = new LinkedList<>();
        paths.addAll(rootPaths);
        paths.addAll(childPaths);
        return paths;
    }

    private List<ResourceTemplates.Path> extractPathFromQueryDescriptor(String version, SpringServiceDescriptor desc) {
        List<ResourceTemplates.Path> rootPaths = desc.getRootActions().stream().map(a -> createRootPathForQuery(version, desc.getRootType(), desc.getPlural(), a)).collect(Collectors.toList());
        List<ResourceTemplates.Path> childPaths = desc.getChildActions().stream().map(p -> createChildPathForQuery(version, desc.getRootType(), desc.getPlural(), p.getKey(), desc.getEntityDescByName(p.getKey()).getPlural(), p.getValue())).collect(Collectors.toList());
        LinkedList<ResourceTemplates.Path> paths = new LinkedList<>();
        paths.addAll(rootPaths);
        paths.addAll(childPaths);
        return paths;
    }

    private ResourceTemplates.Path createRootPathForCommand(String version, String rootType, String plural, SpringServiceDescriptor.ActionDescriptor actionDesc) {
        String action = actionDesc.getName();
        ResourceTemplates.Path path;
        List<ResourceTemplates.Parameter> params = new LinkedList<>();
        String method;
        if (action.equals("Delete" + rootType)) {
            method = "delete";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}");
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        } else if (action.equals("Create" + rootType) && !actionDesc.isIdRequired()) {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json");
            params.add(contentTypeParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        } else if (actionDesc.isIdRequired()) {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            if (action.equals("Update" + rootType)) {
                path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}");
                contentTypeParam.addEnum("application/json");
            } else {
                contentTypeParam.addEnum("application/json;domain-model=" + action);
            }

            params.add(contentTypeParam);
            post.setParameters(params);
        } else {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "#" + action);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json;domain-model=" + action);
            params.add(contentTypeParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        }

        return path;
    }

    private ResourceTemplates.Path createRootPathForQuery(String version, String rootType, String plural, SpringServiceDescriptor.ActionDescriptor actionDesc) {
        String action = actionDesc.getName();
        ResourceTemplates.Path path;
        List<ResourceTemplates.Parameter> params = new LinkedList<>();
        String method;
        if (action.equals("Get" + rootType)) {
            method = "get";

            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}");
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        } else if (action.equals("List" + rootType) && !actionDesc.isIdRequired()) {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json");
            params.add(contentTypeParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        } else if (actionDesc.isIdRequired()) {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json;domain-model=" + action);
            params.add(contentTypeParam);
            get.setParameters(params);
        } else {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + rootType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "#" + action);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json;domain-model=" + action);
            params.add(contentTypeParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        }

        return path;
    }

    private ResourceTemplates.Path createChildPathForCommand(String version, String rootType, String plural, String childType, String childPlural, SpringServiceDescriptor.ActionDescriptor actionDesc) {
        String action = actionDesc.getName();
        ResourceTemplates.Path path;
        List<ResourceTemplates.Parameter> params = new LinkedList<>();
        String method;
        if (action.equals("Delete" + childType)) {
            method = "delete";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "/{" + childType + "Id}");
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter childIdParam = ResourceTemplates.Parameter.builder().in("path").name(childType + "Id").required(true).type("string").build();
            params.add(childIdParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        } else if (action.equals("Create" + childType) && !actionDesc.isIdRequired()) {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json");
            params.add(contentTypeParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        } else if (actionDesc.isIdRequired()) {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "/{" + childType + "Id}#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter childIdParam = ResourceTemplates.Parameter.builder().in("path").name(childType + "Id").required(true).type("string").build();
            params.add(childIdParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            if (action.equals("Update" + childType)) {
                path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "/{" + childType + "Id}");
                contentTypeParam.addEnum("application/json");
            } else {
                contentTypeParam.addEnum("application/json;domain-model=" + action);
            }
            params.add(contentTypeParam);
            post.setParameters(params);
        } else {
            method = "post";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Post.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json;domain-model=" + action);
            params.add(contentTypeParam);
            ResourceTemplates.Post post = (ResourceTemplates.Post) path.getItems().get(method);
            post.setParameters(params);
        }

        return path;
    }

    private ResourceTemplates.Path createChildPathForQuery(String version, String rootType, String plural, String childType, String childPlural, SpringServiceDescriptor.ActionDescriptor actionDesc) {
        String action = actionDesc.getName();
        ResourceTemplates.Path path;
        List<ResourceTemplates.Parameter> params = new LinkedList<>();
        String method;
        if (action.equals("Get" + childType)) {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "/{" + childType + "Id}");
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter childIdParam = ResourceTemplates.Parameter.builder().in("path").name(childType + "Id").required(true).type("string").build();
            params.add(childIdParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        } else if (action.equals("List" + childType) && !actionDesc.isIdRequired()) {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json");
            params.add(contentTypeParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        } else if (actionDesc.isIdRequired()) {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "/{" + childType + "Id}#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter childIdParam = ResourceTemplates.Parameter.builder().in("path").name(childType + "Id").required(true).type("string").build();
            params.add(childIdParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();

            contentTypeParam.addEnum("application/json;domain-model=" + action);

            params.add(contentTypeParam);
            get.setParameters(params);
        } else {
            method = "get";
            path = new ResourceTemplates.Path().withType(method, ResourceTemplates.Get.builder()
                    .operationId(action + "for" + plural + "-" + childType).produces(Collections.singletonList("application/json")).summary(plural).description("api invoke  " + action + " on resource " + childType).tags(Collections.singletonList(plural)).build());
            path.withPath("/" + version + "/" + plural + "/{" + rootType + "Id}/" + childPlural + "#" + action);
            ResourceTemplates.Parameter idParam = ResourceTemplates.Parameter.builder().in("path").name(rootType + "Id").required(true).type("string").build();
            params.add(idParam);
            ResourceTemplates.Parameter contentTypeParam = ResourceTemplates.Parameter.builder().in("header").name("content-type").required(true).Enum(new LinkedList<>()).type("string").build();
            contentTypeParam.addEnum("application/json;domain-model=" + action);
            params.add(contentTypeParam);
            ResourceTemplates.Get get = (ResourceTemplates.Get) path.getItems().get(method);
            get.setParameters(params);
        }

        return path;
    }
}
