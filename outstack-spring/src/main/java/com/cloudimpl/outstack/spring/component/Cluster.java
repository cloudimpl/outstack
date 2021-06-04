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
import com.cloudimpl.outstack.core.Injector;
import com.cloudimpl.outstack.core.ServiceRegistryReadOnly;
import com.cloudimpl.outstack.logger.ConsoleLogWriter;
import com.cloudimpl.outstack.logger.LogWriter;
import com.cloudimpl.outstack.node.CloudNode;
import com.cloudimpl.outstack.runtime.CommandWrapper;
import com.cloudimpl.outstack.runtime.EventRepositoryFactory;
import com.cloudimpl.outstack.runtime.ResourceHelper;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.repo.MemEventRepositoryFactory;
import com.cloudimpl.outstack.spring.service.ServiceDescriptorContextManager;
import java.text.MessageFormat;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    
    private ResourceHelper resourceHelper;
    public Cluster() {
    }

    @PostConstruct
    public void init() {
        Injector injector = new Injector();
        configManager.setInjector(injector);
      //  serviceDescriptorContextMan = new ServiceDescriptorContextManager();
        resourceHelper = new ResourceHelper(configManager.getDomainOwner(), configManager.getDomainContext(), configManager.getApiContext());
        EventRepositoryFactory eventRepoFactory = new MemEventRepositoryFactory(resourceHelper);
        AppConfig appConfig = AppConfig.builder().withGossipPort(configManager.getCluster().getGossipPort())
                .withSeeds(configManager.getCluster().getSeeds().toArray(String[]::new))
                .withSeedName(configManager.getCluster().getSeedName()).withServicePort(configManager.getCluster().getServicePort()).build();
        
        injector.bind(EventRepositoryFactory.class).to(eventRepoFactory);
        injector.bind(ResourceHelper.class).to(resourceHelper);
        injector.bind(ServiceDescriptorContextManager.class).to(serviceDescriptorContextMan);
        injector.bind(LogWriter.class).to(new ConsoleLogWriter());
        //injector.bind(CollectionProvider.class).to(new AwsCollectionProvider("http://localhost:4566"));
        injector.nameBind("leaderOptions", CollectionOptions.builder().withOption("TableName", "Test").build());
        injector.bind(CollectionProvider.class).to(configManager.getProvider(CollectionProvider.class.getName()).getInstance());
        ResourcesLoader serviceLoader = new ResourcesLoaderEx(resourceHelper);
        serviceLoader.preload();
        appConfig.getNodeConfigBuilder().doOnNext(c -> c.withServiceEndpoints(serviceLoader.getEndpoints())).map(C -> C.build())
                .doOnNext(c -> {
                    node = new CloudNode(injector, c);
                    serviceLoader.init(node);
                    node.start();
                }).subscribe();

    }

    @PreDestroy
    public void shutdown() {
        if (node != null) {
            node.shutdown();
        }
        System.exit(-1);
    }

    public ServiceRegistryReadOnly getServiceRegistry()
    {
        return this.node.getServiceRegistry();
    }
    
    public ServiceDescriptorContextManager getServiceDescriptorContextManager() {
        return serviceDescriptorContextMan;
    }

    public <T> Mono<T> requestReply(String serviceName, Object msg) {
   
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
}
