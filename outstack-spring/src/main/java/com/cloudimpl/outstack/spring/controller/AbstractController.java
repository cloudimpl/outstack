package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract controller
 *
 * @author roshanmadhushanka
 **/
public abstract class AbstractController {

    protected final Cluster cluster;

    public AbstractController(Cluster cluster) {
        this.cluster = cluster;
    }

    protected SpringServiceDescriptor getServiceCmdDescriptor(String context, String version, String rootTypePlural) {
        return cluster.getServiceDescriptorContextManager()
                .getCmdServiceDescriptorManager(context, version)
                .flatMap(desc -> desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    protected SpringServiceDescriptor getServiceQueryDescriptor(String context, String version, String rootTypePlural) {
        return cluster.getServiceDescriptorContextManager()
                .getQueryServiceDescriptorManager(context, version)
                .flatMap(desc -> desc.getServiceDescriptorByPlural(rootTypePlural))
                .orElseThrow(() -> new ResourceNotFoundException("resource {0} not found", rootTypePlural));
    }

    protected void validateAction(SpringServiceDescriptor.ActionDescriptor action, SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        if (action.getActionType() != type) {
            throw new BadRequestException("bad request {0}. expect {1} , found {2}", action.getName(), type, action.getActionType());
        }
    }

    protected ResponseEntity<Object> onRootEntityCreation(String context, String version, String rootEntity, Object resource) {
        if (resource instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> response = (LinkedHashMap<?, ?>) resource;
            if (response.containsKey("_id")) {
                return ResponseEntity
                        .created(WebMvcLinkBuilder.linkTo(Controller.class)
                                .slash(context)
                                .slash(version)
                                .slash(rootEntity)
                                .slash(response.get("_id")).toUri())
                        .body(resource);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    protected ResponseEntity<Object> onChildEntityCreation(String context, String version, String rootEntity, String rootId, String childType, Object resource) {
        if (resource instanceof LinkedHashMap) {
            LinkedHashMap<?, ?> response = (LinkedHashMap<?, ?>) resource;
            if (response.containsKey("_id")) {
                return ResponseEntity.created(WebMvcLinkBuilder.linkTo(Controller.class)
                        .slash(context)
                        .slash(version)
                        .slash(rootEntity)
                        .slash(rootId)
                        .slash(childType)
                        .slash(response.get("_id")).toUri())
                        .body(resource);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    protected Throwable onError(Throwable thr) {
        if (thr instanceof ValidationErrorException) {
            return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, thr.getMessage());
        } else if (thr instanceof DomainEventException) {
            DomainEventException de = (DomainEventException) thr;
            switch (de.getErrCode()) {
                case ENTITY_NOT_FOUND -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, thr.getMessage());
                }
                case ENTITY_EXIST -> {
                    return new ResponseStatusException(HttpStatus.CONFLICT, thr.getMessage());
                }
                default -> {
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, thr.getMessage());
                }
            }
        } else if (thr instanceof AuthenticationException) {
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, thr.getMessage());
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, thr.getMessage());
    }

    protected Map<String, String> removePagingParam(Map<String, String> params) {
        params.remove("page");
        params.remove("size");
        params.remove("sort");
        return params;
    }
}
