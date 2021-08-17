package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.QueryWrapper;
import com.cloudimpl.outstack.runtime.ValidationErrorException;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.FileData;
import com.cloudimpl.outstack.runtime.domainspec.Query;
import com.cloudimpl.outstack.spring.component.Cluster;
import com.cloudimpl.outstack.spring.component.SpringServiceDescriptor;
import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import com.cloudimpl.outstack.spring.controller.exception.NotImplementedException;
import com.cloudimpl.outstack.spring.controller.exception.ResourceNotFoundException;
import com.cloudimpl.outstack.spring.util.FileUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    protected Mono<ResponseEntity<Object>> createRootEntity(ServerHttpRequest httpRequest, String context, String version,
                                                            String rootEntity, String contentType, String tenantId,
                                                            String body) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Create" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName()).withPayload(body)
                .withVersion(version)
                .withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request)
                .onErrorMap(this::onError)
                .map(e -> onRootEntityCreation(context, version, rootEntity, e));
    }

    protected Mono<Object> updateRootEntity(ServerHttpRequest httpRequest, String context, String version,
                                            String rootEntity, String rootId, String contentType, String tenantId,
                                            String body) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Update" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} update not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withPayload(body)
                .withId(rootId)
                .withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> uploadRootEntityFiles(ServerHttpRequest httpRequest, String context, String version,
                                                 String rootEntity, String rootId, String contentType, String tenantId,
                                                 List<FilePart> files) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType)
                .orElseThrow(() -> new BadRequestException("domain model is not defined"));

        if (CollectionUtils.isEmpty(files)) {
            throw new BadRequestException("no files were attached to the request");
        }

        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd)
                .filter(SpringServiceDescriptor.ActionDescriptor::isFileUploadEnabled)
                .orElseThrow(() -> new NotImplementedException("resource {0} file upload not implemented", rootType));

        List<FileData> fileDataList = files.stream()
                .map(FileUtil::getFileData)
                .collect(Collectors.toList());
        FileUtil.validateMimeType(fileDataList, action.getMimeTypes());

        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withFiles(fileDataList.stream().map(e -> (Object) e).collect(Collectors.toList()))
                .withId(rootId)
                .withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<ResponseEntity<Object>> createChildEntity(ServerHttpRequest httpRequest, String context,
                                                             String version, String rootEntity,
                                                             String rootId, String childEntity, String contentType,
                                                             String tenantId, String body) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Create" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} creation not implemented",
                        child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withPayload(body)
                .withRootId(rootId)
                .withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request)
                .onErrorMap(this::onError)
                .map(r -> this.onChildEntityCreation(context, version, rootEntity, rootId, childEntity, r));
    }

    protected Mono<Object> updateChildEntity(ServerHttpRequest httpRequest, String context, String version,
                                             String rootEntity, String rootId, String childEntity, String childId,
                                             String contentType, String tenantId, String body) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Update" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} update not implemented",
                        child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withPayload(body)
                .withId(childId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<ResponseEntity<Object>> uploadChildEntityFiles(ServerHttpRequest httpRequest, String context,
                                                                  String version, String rootEntity,
                                                                  String rootId, String childEntity, String childId,
                                                                  String contentType, String tenantId,
                                                                  List<FilePart> files) {

        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType)
                .orElseThrow(() -> new BadRequestException("domain model is not defined"));

        if (CollectionUtils.isEmpty(files)) {
            throw new BadRequestException("no files were attached to the request");
        }

        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .filter(SpringServiceDescriptor.ActionDescriptor::isFileUploadEnabled)
                .orElseThrow(() -> new NotImplementedException("resource {0} file upload not implemented",
                        child.getName()));

        List<FileData> fileDataList = files.stream()
                .map(FileUtil::getFileData)
                .collect(Collectors.toList());
        FileUtil.validateMimeType(fileDataList, action.getMimeTypes());

        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withFiles(fileDataList.stream().map(e -> (Object) e).collect(Collectors.toList()))
                .withId(childId)
                .withRootId(rootId)
                .withTenantId(tenantId)
                .build();

        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError)
                .map(r -> this.onChildEntityCreation(context, version, rootEntity, rootId, childEntity, r));
    }

    protected Mono<Object> getRootEntity(ServerHttpRequest httpRequest, String context, String version,
                                         String rootEntity, String rootId, String contentType, String tenantId, Pageable pageable,
                                         Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));
        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElseGet(() -> "Get" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query)
                .orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withVersion(version)
                .withQuery(action.getName())
                .withId(rootId).withRootId(rootId)
                .withPageRequest(pagingReq)
                .withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> getRootEntityEvents(ServerHttpRequest httpRequest, String context,  String version,
                                               String rootEntity, String rootId, String contentType, String tenantId,
                                               Pageable pageable,
                                             Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));
        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElseGet(() -> "Get" + rootType + "Events");
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query)
                .orElseThrow(() -> new NotImplementedException("resource  {0} get events not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withVersion(version)
                .withQuery(action.getName())
                .withId(rootId).withRootId(rootId)
                .withTenantId(tenantId)
                .withPageRequest(pagingReq).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> getChildEntity(ServerHttpRequest httpRequest, String context, String version,
                                          String rootEntity, String rootId, String childEntity, String childId,
                                          String contentType, String tenantId, Pageable pageable,
                                          Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));
        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Get" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} get not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withQuery(action.getName())
                .withVersion(version)
                .withPageRequest(pagingReq)
                .withRootId(rootId)
                .withId(childId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> getChildEntityEvents(ServerHttpRequest httpRequest, String context, String version,
                                                String rootEntity, String rootId, String childEntity, String childId,
                                                String contentType, String tenantId, Pageable pageable,
                                                Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Get" + child.getName() + "Events");
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} get events not implemented",
                        child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withQuery(action.getName())
                .withVersion(version)
                .withRootId(rootId)
                .withId(childId)
                .withTenantId(tenantId)
                .withPageRequest(pagingReq)
                .build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> listChildEntity(ServerHttpRequest httpRequest, String context, String version,
                                           String rootEntity, String rootId, String childEntity, String contentType,
                                           String tenantId, Pageable pageable, Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "List" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource  {0} list not implemented", child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withQuery(action.getName())
                .withVersion(version)
                .withRootId(rootId)
                .withTenantId(tenantId).withPageRequest(pagingReq).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> listRootEntity(ServerHttpRequest httpRequest, String context, String version,
                                          String rootEntity, String contentType, String tenantId, Pageable pageable,
                                          Map<String, String> reqParam) {
        Query.PagingRequest pagingReq = new Query.PagingRequest(pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().get()
                        .map(o -> new Query.Order(o.getProperty(), o.getDirection() == Sort.Direction.ASC
                                ? Query.Direction.ASC : Query.Direction.DESC)).collect(Collectors.toList()),
                removePagingParam(reqParam));

        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElseGet(() -> "List" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query)
                .orElseThrow(() -> new NotImplementedException("resource  {0} list not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withQuery(action.getName())
                .withVersion(version)
                .withTenantId(tenantId).withPageRequest(pagingReq).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> deleteChildEntity(ServerHttpRequest httpRequest, String context, String version,
                                             String rootEntity, String rootId, String childEntity, String childId,
                                             String contentType, String tenantId) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        SpringServiceDescriptor.EntityDescriptor child = serviceDesc.getEntityDescriptorByPlural(childEntity)
                .orElseThrow(() -> new ResourceNotFoundException("resource {0}/{1}/{2} not found",
                        rootEntity, rootId, childEntity));
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Delete" + child.getName());
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getChildAction(child.getName(), cmd)
                .orElseThrow(() -> new NotImplementedException("resource {0} deletion not implemented",
                        child.getName()));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withRootId(rootId).withId(childId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Mono<Object> deleteRootEntity(ServerHttpRequest httpRequest, String context, String version,
                                            String rootEntity, String rootId, String contentType, String tenantId) {
        SpringServiceDescriptor serviceDesc = getServiceCmdDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String cmd = DomainModelDecoder.decode(contentType).orElseGet(() -> "Delete" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(cmd)
                .orElseThrow(() -> new NotImplementedException("resource {0} deletion not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.COMMAND_HANDLER);
        CommandWrapper request = CommandWrapper.builder()
                .withCommand(action.getName())
                .withVersion(version)
                .withRootId(rootId).withId(rootId).withTenantId(tenantId).build();
        return cluster.requestReply(httpRequest, serviceDesc.getServiceName(), request).onErrorMap(this::onError);
    }

    protected Flux<String> getRootEntityStream(String context, String version,
                                               String rootEntity, String rootId, String contentType, String tenantId) {
        SpringServiceDescriptor serviceDesc = getServiceQueryDescriptor(context, version, rootEntity);
        String rootType = serviceDesc.getRootType();
        String query = DomainModelDecoder.decode(contentType).orElseGet(() -> "Get" + rootType);
        SpringServiceDescriptor.ActionDescriptor action = serviceDesc.getRootAction(query)
                .orElseThrow(() -> new NotImplementedException("resource  {0} get stream not implemented", rootType));
        validateAction(action, SpringServiceDescriptor.ActionDescriptor.ActionType.QUERY_HANDLER);
        QueryWrapper request = QueryWrapper.builder()
                .withVersion(version)
                .withQuery(action.getName())
                .withId(rootId).withRootId(rootId).withTenantId(tenantId).build();
        return cluster.requestStream(serviceDesc.getServiceName(), request).map(s -> GsonCodec.encode(s))
                .onErrorMap(this::onError);
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

    protected void validateAction(SpringServiceDescriptor.ActionDescriptor action,
                                  SpringServiceDescriptor.ActionDescriptor.ActionType type) {
        if (action.getActionType() != type) {
            throw new BadRequestException("bad request {0}. expect {1} , found {2}", action.getName(), type,
                    action.getActionType());
        }
    }

    protected ResponseEntity<Object> onRootEntityCreation(String context, String version, String rootEntity,
                                                          Object resource) {
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

    protected ResponseEntity<Object> onChildEntityCreation(String context, String version, String rootEntity,
                                                           String rootId, String childType, Object resource) {
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
