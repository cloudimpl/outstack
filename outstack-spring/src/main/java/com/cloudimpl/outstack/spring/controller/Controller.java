/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.QueryWrapper;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import com.cloudimpl.outstack.spring.controller.exception.NotImplementedException;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorManager;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.ResponseEntity.created;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private Mono<Object> createRootEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withTenantId(tenantId).build();
        //   created(URI.create(MessageFormat.format("{0}/{1}", rootType,)))
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> updateRootEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElseThrow(() -> new BadRequestException("missing domain model"));
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> createChildEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> updateChildEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseThrow(() -> new BadRequestException("missing domain model"));
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withId(childId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getRootEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context,version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElse("Get" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> getChildEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context,version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Get" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withRootId(rootId).withId(childId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> listChildEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context,version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("List" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> deleteChildEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Delete" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withRootId(rootId).withId(childId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    private Mono<Object> deleteRootEntity(@PathVariable String context,@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType,@RequestHeader(name = "X-TenantId",required = false) String tenantId) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context,version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElse("Delete" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withRootId(rootId).withId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }
    
    @GetMapping("/stream")
    @SuppressWarnings("unused")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i -> "tick" + i + "\n");
    }

    private SpringServiceDescriptor getServiceCmdDescriptor(String context,String version, String rootTypePlural) {
        return cluster.getServiceDescriptorContextManager()
                .getCmdServiceDescriptorManager(context, version)
                .flatMap(desc->desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }
    
     private SpringServiceDescriptor getServiceQueryDescriptor(String context,String version, String rootTypePlural) {
       return cluster.getServiceDescriptorContextManager()
                .getQueryServiceDescriptorManager(context, version)
                .flatMap(desc->desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    private void validateAction(SpringServiceDescriptor.ActionDescriptor action, SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        if (action.getActionType() != type) {
            throw new BadRequestException("bad request {0}. expect {1} , found {2}", action.getName(), type, action.getActionType());
        }
    }
    
    private Throwable onError(Throwable thr)
    {
        if(ValidationErrorException.class.isInstance(thr))
        {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,thr.getMessage());
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,thr.getMessage());
    }
}
