package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Provides public accessible endpoints
 *
 * @author roshanmadhushanka
 **/
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*",methods = {RequestMethod.DELETE,RequestMethod.GET,RequestMethod.POST})
@RequestMapping("/public")
public class PublicController extends AbstractController {

    public PublicController(Cluster cluster) {
        super(cluster);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> updateRootEntity(@PathVariable String context, @PathVariable String version,
                                            @PathVariable String rootEntity, @PathVariable String rootId,
                                            @RequestHeader("Content-Type") String contentType,
                                            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
                                            @RequestBody String body) {
        return super.updateRootEntity(context, version, rootEntity, rootId, contentType, tenantId, body);
    }

    @Override
    protected void validateAction(SpringServiceDescriptor.ActionDescriptor action, SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        super.validateAction(action, type);

        if(!action.isPubliclyAccessible()) {
            throw new BadRequestException("action {0} is restricted to access through public", action.getName());
        }
    }
}
