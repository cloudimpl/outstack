/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.QueryWrapper;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.runtime.common.GsonCodec;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import com.cloudimpl.outstack.spring.controller.exception.NotImplementedException;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author nuwan
 */
@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    Cluster cluster;

    @PostMapping(value = "{context}/{version}/{rootEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> createRootEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, @RequestBody String body) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName()).withPayload(body)
                    .withVersion(version)
                    .withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError).map(r -> this.onRootEntityCreation(context, version, rootEntity, r));
        });
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> updateRootEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, @RequestBody String body) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String cmd = DomainModelDecoder.decode(contentType).orElse("Update" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName())
                    .withVersion(version)
                    .withPayload(body)
                    .withId(rootId)
                    .withRootId(rootId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> createChildEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, @RequestBody String body) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + child.getName());
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName())
                    .withVersion(version)
                    .withPayload(body)
                    .withRootId(rootId)
                    .withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError).map(r -> this.onChildEntityCreation(context, version, rootEntity, rootId, childEntity, r));
        });
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> updateChildEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, @RequestBody String body) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("Update" + child.getName());
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName())
                    .withVersion(version)
                    .withPayload(body)
                    .withId(childId).withRootId(rootId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getRootEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String query = DomainModelDecoder.decode(contentType).orElse("Get" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withVersion(version)
                    .withQuery(action.getName())
                    .withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/events", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getRootEntityEvents(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity,
            @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return doAuth().flatMap(token -> {
            Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort().get().map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()), removePaginParam(reqParam));
            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String query = DomainModelDecoder.decode(contentType).orElse("Get" + rootType + "Events");
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withVersion(version)
                    .withQuery(action.getName())
                    .withId(rootId).withRootId(rootId)
                    .withTenantId(tenantId)
                    .withPageRequest(pagingReq).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getChildEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("Get" + child.getName());
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withQuery(action.getName())
                    .withVersion(version)
                    .withRootId(rootId)
                    .withId(childId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}/events", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getChildEntityEvents(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity,
            @PathVariable String childId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId, Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return doAuth().flatMap(token -> {
            Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort().get().map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()), removePaginParam(reqParam));

            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("Get" + child.getName() + "Events");
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withQuery(action.getName())
                    .withVersion(version)
                    .withRootId(rootId)
                    .withId(childId)
                    .withTenantId(tenantId)
                    .withPageRequest(pagingReq)
                    .build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> listChildEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId, Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return doAuth().flatMap(token -> {
            Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort().get().map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()), removePaginParam(reqParam));

            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("List" + child.getName());
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withQuery(action.getName())
                    .withVersion(version)
                    .withRootId(rootId)
                    .withTenantId(tenantId).withPageRequest(pagingReq).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> listRootEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId, Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return doAuth().flatMap(token -> {
            Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                    pageable.getSort().get().map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()), removePaginParam(reqParam));

            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String query = DomainModelDecoder.decode(contentType).orElse("List" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withQuery(action.getName())
                    .withVersion(version)
                    .withTenantId(tenantId).withPageRequest(pagingReq).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> deleteChildEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
            String cmd = DomainModelDecoder.decode(contentType).orElse("Delete" + child.getName());
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource {0} creation not implemented", child.getName()));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName())
                    .withVersion(version)
                    .withRootId(rootId).withId(childId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> deleteRootEntity(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return doAuth().flatMap(token -> {
            SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String cmd = DomainModelDecoder.decode(contentType).orElse("Delete" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource {0} deletion not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
            CommandWrapper request = CommandWrapper.builder()
                    .withCommand(action.getName())
                    .withVersion(version)
                    .withRootId(rootId).withId(rootId).withTenantId(tenantId).build();
            return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
        });
    }

    @GetMapping("/stream")
    @SuppressWarnings("unused")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i -> "tick" + i + "\n");
    }

    @GetMapping(value = "/streams/{context}/{version}/{rootEntity}/{rootId}")
    @SuppressWarnings("unused")
    private Flux<String> getRootEntityStream(@PathVariable String context, @PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return doAuth().flatMapMany(token -> {
            SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
            String rootType = serviceDesc.getRootType();
            String query = DomainModelDecoder.decode(contentType).orElse("Get" + rootType);
            SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
            validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
            QueryWrapper request = QueryWrapper.builder()
                    .withVersion(version)
                    .withQuery(action.getName())
                    .withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
            return cluster.requestStream(serviceDesc.getServiceName(), request).map(s -> GsonCodec.encode(s)).onErrorMap(this::onError);
        });
    }

    private SpringServiceDescriptor getServiceCmdDescriptor(String context, String version, String rootTypePlural) {
        return cluster.getServiceDescriptorContextManager()
                .getCmdServiceDescriptorManager(context, version)
                .flatMap(desc -> desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    private SpringServiceDescriptor getServiceQueryDescriptor(String context, String version, String rootTypePlural) {
        return cluster.getServiceDescriptorContextManager()
                .getQueryServiceDescriptorManager(context, version)
                .flatMap(desc -> desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    private void validateAction(SpringServiceDescriptor.ActionDescriptor action, SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        if (action.getActionType() != type) {
            throw new BadRequestException("bad request {0}. expect {1} , found {2}", action.getName(), type, action.getActionType());
        }
    }

    private Object onRootEntityCreation(String context, String version, String rootEntity, Object resource) {
        if (Entity.class.isInstance(resource)) {
            //return created(URI.create(MessageFormat.format("{0}/{1}/{2}/{3}", context, version, rootEntity, Entity.class.cast(resource).id()))).build();
            return created(WebMvcLinkBuilder.linkTo(Controller.class).slash(context).slash(version).slash(rootEntity).slash(Entity.class.cast(resource).id()).toUri()).build();
        } else {
            return resource;
        }
    }

    private Object onChildEntityCreation(String context, String version, String rootEntity, String rootId, String childType, Object resource) {
        if (Entity.class.isInstance(resource)) {
            //return created(URI.create(MessageFormat.format("{0}/{1}/{2}/{3}", context, version, rootEntity, Entity.class.cast(resource).id()))).build();
            return created(WebMvcLinkBuilder.linkTo(Controller.class).slash(context).slash(version).slash(rootEntity).slash(rootId).slash(childType).slash(Entity.class.cast(resource).id()).toUri()).build();
        } else {
            return resource;
        }
    }

    private Throwable onError(Throwable thr) {
        if (ValidationErrorException.class.isInstance(thr)) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, thr.getMessage());
        } else if (DomainEventException.class.isInstance(thr)) {
            DomainEventException de = (DomainEventException) thr;
            switch (de.getErrCode()) {
                case ENTITY_NOT_FOUND: {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, thr.getMessage());
                }
                case ENTITY_EXIST: {
                    return new ResponseStatusException(HttpStatus.CONFLICT, thr.getMessage());
                }
                default: {
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, thr.getMessage());
                }
            }
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, thr.getMessage());
    }

    private Map<String, String> removePaginParam(Map<String, String> params) {
        params.remove("page");
        params.remove("size");
        params.remove("sort");
        return params;
    }

    @GetMapping("/doAuth")
    private Mono<AbstractAuthenticationToken> doAuth() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(c -> AbstractAuthenticationToken.class.cast(c.getAuthentication())).doOnError(err -> err.printStackTrace());
    }
}
