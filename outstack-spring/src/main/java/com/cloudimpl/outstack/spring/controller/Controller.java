/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.QueryWrapper;
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

/**
 *
 * @author nuwan
 */
@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    Cluster cluster;

    @PostMapping(value = "api/{version}/{rootEntity}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> createRootEntity(@PathVariable String version, @PathVariable String rootEntity, @RequestHeader("Content-Type") String contentType, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).build();
        //   created(URI.create(MessageFormat.format("{0}/{1}", rootType,)))
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @PostMapping(value = "api/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> updateRootEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElseThrow(() -> new BadRequestException("missing domain model"));
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withRootId(rootId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @PostMapping(value = "api/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> createChildEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Create" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withRootId(rootId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @PostMapping(value = "api/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> updateChildEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType, @RequestBody String body) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseThrow(() -> new BadRequestException("missing domain model"));
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withPayload(body).withRootId(rootId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @GetMapping(value = "api/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> getRootEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @RequestHeader("Content-Type") String contentType) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElse("Get" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query).orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withId(rootId).withRootId(rootId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @GetMapping(value = "api/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> getChildEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Get" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withRootId(rootId).withId(childId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @GetMapping(value = "api/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> listChildEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType) {

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("List" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder().withQuery(action.getName()).withRootId(rootId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @DeleteMapping(value = "api/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}", consumes = {APPLICATION_JSON_VALUE})
    private Mono<String> deleteChildEntity(@PathVariable String version, @PathVariable String rootEntity, @PathVariable String rootId, @PathVariable String childEntity, @PathVariable String childId, @RequestHeader("Content-Type") String contentType) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity).orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found", rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElse("Delete" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd).orElseThrow(() -> new NotImplementedException("resource {0} creation not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder().withCommand(action.getName()).withRootId(rootId).withId(childId).build();
        return cluster.requestReply(serviceDesc.getServiceName(), request);
    }

    @GetMapping("/stream")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i -> "tick" + i + "\n");
    }

    private ServiceDescriptorManager getSrvCmdDescByVersion(String version) {
        return cluster.getServiceDescriptorManager().getVersionForCmd(version).orElseThrow(() -> new ResourceNotFoundException("api version {0} not found", version));
    }

    private ServiceDescriptorManager getSrvQueryDescByVersion(String version) {
        return cluster.getServiceDescriptorManager().getVersionForQuery(version).orElseThrow(() -> new ResourceNotFoundException("api version {0} not found", version));
    }

    private SpringServiceDescriptor getServiceDescriptor(ServiceDescriptorManager man, String rootTypePlural) {
        return man.getServiceDescriptorByPlural(rootTypePlural).orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    private SpringServiceDescriptor getServiceCmdDescriptor(String version, String rootTypePlural) {
        return getServiceDescriptor(getSrvCmdDescByVersion(version), rootTypePlural);
    }
    
     private SpringServiceDescriptor getServiceQueryDescriptor(String version, String rootTypePlural) {
        return getServiceDescriptor(getSrvQueryDescByVersion(version), rootTypePlural);
    }

    private void validateAction(SpringServiceDescriptor.ActionDescriptor action, SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        if (action.getActionType() != type) {
            throw new BadRequestException("bad request {0}. expect {1} , found {2}", action.getName(), type, action.getActionType());
        }
    }
}
