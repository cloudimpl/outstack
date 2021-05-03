/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.app.AppConfig;
import com.cloudimpl.outstack.app.ResourcesLoader;
import com.cloudimpl.outstack.collection.AwsCollectionProvider;
import com.cloudimpl.outstack.collection.CollectionOptions;
import com.cloudimpl.outstack.collection.CollectionProvider;
import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.CloudMessageDecoder;
import com.cloudimpl.outstack.common.CloudMessageEncoder;
import com.cloudimpl.outstack.core.Injector;
import com.cloudimpl.outstack.logger.ConsoleLogWriter;
import com.cloudimpl.outstack.logger.LogWriter;
import com.cloudimpl.outstack.node.CloudNode;
import com.cloudimpl.outstack.runtime.common.GsonCodec;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${com.cloudimpl.outstack.cluster.gossipPort:12000}")
    private int gossipPort;

    @Value("${com.cloudimpl.outstack.cluster.seedName:@null}")
    private String seedName;

    @Value("${com.cloudimpl.outstack.cluster.servicePort:10000}")
    private int servicePort;

    private CloudNode node;

    public Cluster() {
    }

    @PostConstruct
    public void init() {
        AppConfig appConfig = AppConfig.builder().withGossipPort(gossipPort).withSeedName(seedName).withServicePort(servicePort).build();
        Injector injector = new Injector();
        injector.bind(LogWriter.class).to(new ConsoleLogWriter());
        injector.bind(CollectionProvider.class).to(new AwsCollectionProvider("http://localhost:4566"));
        injector.nameBind("leaderOptions", CollectionOptions.builder().withOption("TableName", "Test").build());
        ResourcesLoader serviceLoader = new ResourcesLoaderEx();
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

    public <T> Mono<T> requestReply(String serviceName, Object msg) {
        return this.node.requestReply(serviceName, msg);
    }

    public <T> Flux<T> requestStream(String serviceName, Object msg) {
        return this.node.requestStream(serviceName, msg);
    }

    public Mono<Void> send(String serviceName, Object msg) {
        return this.node.send(serviceName, msg);
    }
}
