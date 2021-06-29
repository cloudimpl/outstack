/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.app.AppConfig;
import com.cloudimpl.outstack.app.ResourcesLoader;
import com.cloudimpl.outstack.collection.CollectionOptions;
import com.cloudimpl.outstack.collection.CollectionProvider;
import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.CloudMessageDecoder;
import com.cloudimpl.outstack.common.CloudMessageEncoder;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.CloudUtil;
import com.cloudimpl.outstack.core.Injector;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import com.cloudimpl.outstack.logger.ConsoleLogWriter;
import com.cloudimpl.outstack.logger.LogWriter;
import com.cloudimpl.outstack.node.CloudNode;
import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.CommandWrapperHelper;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;
import com.cloudimpl.outstack.spring.security.PlatformAuthenticationToken;
import com.cloudimpl.outstack.spring.security.PolicyStatementValidator;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorContextManager;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
@Component("OUTSTACK_CLUSTER")
public class Cluster {

    static {
        GsonCodec.registerTypeAdaptor(CloudMessage.class, () -> new CloudMessageDecoder(), () -> new CloudMessageEncoder());
    }

    private CloudNode node;

    @Autowired
    private ServiceDescriptorContextManager serviceDescriptorContextMan;

    @Autowired
    private SpringApplicationConfigManager configManager;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private ResourceHelper resourceHelper;

    public Cluster() {
    }

    private static AutowireCapableBeanFactory beanFactoryInstance;

    @PostConstruct
    public void init() {
        beanFactoryInstance = beanFactory;
        Injector injector = new Injector();
        configManager.setInjector(injector);
        //  serviceDescriptorContextMan = new ServiceDescriptorContextManager();
        resourceHelper = new ResourceHelper(configManager.getDomainOwner(), configManager.getDomainContext(), configManager.getApiContext());
        injector.bind(ResourceHelper.class).to(resourceHelper);
        //   EventRepositoryFactory eventRepoFactory = new MemEventRepositoryFactory(resourceHelper);
        AppConfig appConfig = AppConfig.builder().withGossipPort(configManager.getCluster().getGossipPort())
                .withSeeds(configManager.getCluster().getSeeds().toArray(String[]::new))
                .withSeedName(configManager.getCluster().getSeedName()).withServicePort(configManager.getCluster().getServicePort()).build();

        //   injector.bind(EventRepositoryFactory.class).to(eventRepoFactory);
        injector.bind(ServiceDescriptorContextManager.class).to(serviceDescriptorContextMan);
        injector.bind(LogWriter.class).to(new ConsoleLogWriter());
        bindVars(injector);
        //injector.bind(CollectionProvider.class).to(new AwsCollectionProvider("http://localhost:4566"));
        injector.nameBind("leaderOptions", CollectionOptions.builder().withOption("TableName", "Test").build());
        //injector.bind(CollectionProvider.class).to(configManager.getProvider(CollectionProvider.class.getName()).get().getInstance());
        ResourcesLoader serviceLoader = new ResourcesLoaderEx(resourceHelper);
        serviceLoader.preload();
        appConfig.getNodeConfigBuilder().doOnNext(c -> c.withServiceEndpoints(serviceLoader.getEndpoints())).map(C -> C.build())
                .doOnNext(c -> {
                    node = new CloudNode(injector, c);
                    serviceLoader.init(node);
                    node.start();
                }).subscribe();

    }

    private void bindVars(Injector injector) {
        configManager.getProviders().stream().forEach(p -> {
            if (p.getStatus().isPresent() && p.getStatus().get().equals("active")) {
                Object instance = p.getInstance();
                injector.bind(CloudUtil.classForName(p.getBase())).to(instance);
                injector.bind(p.getName()).to(instance);
            } else {
                injector.bind(p.getName()).toClass(CloudUtil.classForName(p.getImpl()));
            }
        });
    }

    public static Consumer<Object> autoWireInstance() {
        return beanFactoryInstance::autowireBean;

    }

    @PreDestroy
    public void shutdown() {
        if (node != null) {
            node.shutdown();
        }
        System.exit(-1);
    }

    public ServiceRegistryReadOnly getServiceRegistry() {
        return this.node.getServiceRegistry();
    }

    public ServiceDescriptorContextManager getServiceDescriptorContextManager() {
        return serviceDescriptorContextMan;
    }

    public <T> Mono<T> requestReply(ServerHttpRequest httpRequest, String serviceName, Object msg) {
        if (msg instanceof CommandWrapper) {
            CommandWrapper wrapper = CommandWrapper.class.cast(msg);
            return ReactiveSecurityContextHolder
                    .getContext().map(c -> c.getAuthentication())
                    .cast(PlatformAuthenticationToken.class)               
            .doOnNext(c -> populateExternalAttributes(c, httpRequest, wrapper))
                    .map(t -> PolicyStatementValidator.processPolicyStatements(wrapper.commandName(), wrapper.getRootType(), t))
                    .doOnNext(g -> wrapper.setGrant(g))
                    .flatMap(g -> this.node.requestReply(serviceName, msg))
                    .switchIfEmpty(Mono.defer(() -> this.node.requestReply(serviceName, msg)))
                    .map(o -> (T) o);
        }

        return this.node.requestReply(serviceName, msg);
    }

    public <T> Flux<T> requestStream(String serviceName, Object msg) {
        return this.node.requestStream(serviceName, msg);
    }

    public Mono<Void> send(String serviceName, Object msg) {
        return this.node.send(serviceName, msg);
    }

//    public <T> Mono<T> requestReplyToServiceProvider(String serviceName,Object msg)
//    {
//        return requestReply(MessageFormat.format("{0}/{1}/{2}", resourceHelper.getDomainOwner(),resourceHelper.getDomainContext(),serviceName), msg);
//    }
//    
//    public <T> Mono<T> requestReplyToServiceProvider(String domainOwner,String domainContext,String version,String serviceName,Object msg)
//    {
//        return requestReply(MessageFormat.format("{0}/{1}/{2}/serviceName", domainOwner,domainContext), msg);
//    }
    private void populateExternalAttributes(PlatformAuthenticationToken token, ServerHttpRequest httpRequest, CommandWrapper wrapper) {
        Map<String, String> mapAttr = new HashMap<>();
        if (httpRequest != null) {
            mapAttr.put("@remoteIp", httpRequest.getRemoteAddress().toString());
        }
        if (token.getJwtToken().getClaim("userId") != null) {
            mapAttr.put("@userId", token.getJwtToken().getClaim("userId"));
        }
        if (token.getJwtToken().getClaim("userName") != null) {
            mapAttr.put("@userName", token.getJwtToken().getClaim("userName"));
        }
        CommandWrapperHelper.withMapAttr(wrapper, mapAttr);
    }
}
