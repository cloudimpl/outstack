/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.spring.component.Cluster;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 *
 * @author nuwan
 */
@RestController

@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST})
@RequestMapping(path = "/managed")
public class Controller extends AbstractController {

    public Controller(Cluster cluster) {
        super(cluster);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    @ResponseStatus(HttpStatus.CREATED)
    protected Mono<ResponseEntity<Object>> createRootEntity(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestBody String body) {
        return super.createRootEntity(request, context, version, rootEntity, contentType, tenantId, body);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> updateRootEntity(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestBody String body) {
        return super.updateRootEntity(request, context, version, rootEntity, rootId, contentType, tenantId, body);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/files", consumes = {MULTIPART_FORM_DATA_VALUE})
    @SuppressWarnings("unused")
    @ResponseStatus(HttpStatus.OK)
    protected Mono<Object> uploadRootEntityFiles(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity,
            @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestPart("files") List<FilePart> files) {
        return super.uploadRootEntityFiles(request, context, version, rootEntity, rootId, contentType, tenantId, files);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    @ResponseStatus(HttpStatus.CREATED)
    protected Mono<ResponseEntity<Object>> createChildEntity(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @PathVariable String childEntity,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestBody String body) {
        return super.createChildEntity(request, context, version, rootEntity, rootId, childEntity, contentType, tenantId, body);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}",
            consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> updateChildEntity(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @PathVariable String childEntity, @PathVariable String childId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestBody String body) {
        return super.updateChildEntity(request, context, version, rootEntity, rootId, childEntity, childId, contentType,
                tenantId, body);
    }

    @PostMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}/files",
            consumes = {MULTIPART_FORM_DATA_VALUE})
    @SuppressWarnings("unused")
    @ResponseStatus(HttpStatus.OK)
    protected Mono<ResponseEntity<Object>> uploadChildEntityFiles(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity,
            @PathVariable String rootId,
            @PathVariable String childEntity,
            @PathVariable String childId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            @RequestPart("files") List<FilePart> files) {
        return super.uploadChildEntityFiles(request, context, version, rootEntity, rootId, childEntity, childId, contentType,
                tenantId, files);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> getRootEntity(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.getRootEntity(request, context, version, rootEntity, rootId, contentType, tenantId, pageable, reqParam);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/events", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> getRootEntityEvents(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.getRootEntityEvents(request, context, version, rootEntity, rootId, contentType, tenantId, pageable,
                reqParam);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}",
            consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> getChildEntity(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @PathVariable String childEntity, @PathVariable String childId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.getChildEntity(request, context, version, rootEntity, rootId, childEntity, childId, contentType, tenantId, pageable, reqParam);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}/events",
            consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> getChildEntityEvents(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version, @PathVariable String rootEntity,
            @PathVariable String rootId, @PathVariable String childEntity,
            @PathVariable String childId, @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.getChildEntityEvents(request, context, version, rootEntity, rootId, childEntity, childId, contentType,
                tenantId, pageable, reqParam);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> listChildEntity(ServerHttpRequest request, @PathVariable String context, @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @PathVariable String childEntity, @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.listChildEntity(request, context, version, rootEntity, rootId, childEntity, contentType, tenantId, pageable,
                reqParam);
    }

    @GetMapping(value = "{context}/{version}/{rootEntity}")
    @SuppressWarnings("unused")
    protected Mono<Object> listRootEntity(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version, @PathVariable String rootEntity,
            @RequestHeader(name = "Content-Type",required = false) String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId,
            Pageable pageable, @RequestParam Map<String, String> reqParam) {
        return super.listRootEntity(request, context, version, rootEntity, contentType, tenantId, pageable, reqParam);
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}/{childEntity}/{childId}",
            consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> deleteChildEntity(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @PathVariable String childEntity, @PathVariable String childId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return super.deleteChildEntity(request, context, version, rootEntity, rootId, childEntity, childId, contentType,
                tenantId);
    }

    @DeleteMapping(value = "{context}/{version}/{rootEntity}/{rootId}", consumes = {APPLICATION_JSON_VALUE})
    @SuppressWarnings("unused")
    protected Mono<Object> deleteRootEntity(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return super.deleteRootEntity(request, context, version, rootEntity, rootId, contentType, tenantId);
    }

    @GetMapping("/stream")
    @SuppressWarnings("unused")
    private Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(i -> "tick" + i + "\n");
    }

    @GetMapping(value = "/streams/{context}/{version}/{rootEntity}/{rootId}")
    @SuppressWarnings("unused")
    protected Flux<String> getRootEntityStream(ServerHttpRequest request, @PathVariable String context,
            @PathVariable String version,
            @PathVariable String rootEntity, @PathVariable String rootId,
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(name = "X-TenantId", required = false) String tenantId) {
        return super.getRootEntityStream(request, context, version, rootEntity, rootId, contentType, tenantId);
    }

    @GetMapping("/doAuth")
    private Mono<AbstractAuthenticationToken> doAuth() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(c -> (AbstractAuthenticationToken) c.getAuthentication())
                .doOnError(Throwable::printStackTrace);
    }

    @GetMapping("/hello")
    private Mono<String> hello() {
        return ReactiveSecurityContextHolder
                .getContext()
                .map(c -> (AbstractAuthenticationToken) c.getAuthentication())
                .doOnError(Throwable::printStackTrace).map(t -> "hello " + t.getName());
    }

//    @GetMapping(path = "/v2/outstack-api-docs",consumes = {MediaType.ALL_VALUE})
//    public Mono<String> apiDoc() {
//        return Mono.just("{\n"
//                + "   \"swagger\":\"2.0\",\n"
//                + "   \"info\":{\n"
//                + "      \"description\":\"swagger documentation  for no-code rest point in outstack microservice engine\",\n"
//                + "      \"version\":\"1.0\",\n"
//                + "      \"title\":\"Swagger By CloudImpl COM\",\n"
//                + "      \"contact\":{\n"
//                + "         \"name\":\"CloudImpl COM\",\n"
//                + "         \"url\":\"http://www.cloudimpl.com\",\n"
//                + "         \"email\":\"info@cloudimpl.com\"\n"
//                + "      },\n"
//                + "      \"license\":{\n"
//                + "         \"name\":\"Apache License Version 2.0\"\n"
//                + "      }\n"
//                + "   },\n"
//                + "   \"host\":\"localhost:9098\",\n"
//                + "   \"basePath\":\"/\",\n"
//                + "   \"tags\":[\n"
//                + "      {\n"
//                + "         \"name\":\"abc\",\n"
//                + "         \"description\":\"sample dtag\"\n"
//                + "      },\n"
//                + "      {\n"
//                + "         \"name\":\"auth-controller\",\n"
//                + "         \"description\":\"Auth Controller\"\n"
//                + "      },\n"
//                + "      {\n"
//                + "         \"name\":\"controller\",\n"
//                + "         \"description\":\"Controller\"\n"
//                + "      },\n"
//                + "      {\n"
//                + "         \"name\":\"login-form-controller\",\n"
//                + "         \"description\":\"Login Form Controller\"\n"
//                + "      },\n"
//                + "      {\n"
//                + "         \"name\":\"public-controller\",\n"
//                + "         \"description\":\"Public Controller\"\n"
//                + "      }\n"
//                + "   ]}");
//    }
}
