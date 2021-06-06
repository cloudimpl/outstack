/*
 * Copyright 2020 nuwansa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.app;

import com.cloudimpl.outstack.common.RetryUtil;
import com.cloudimpl.outstack.core.CloudException;
import com.cloudimpl.outstack.node.NodeConfig;
import io.scalecube.net.Address;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import picocli.CommandLine;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

/**
 *
 * @author nuwansa
 */
public class AppConfig implements Callable<Integer> {

    @CommandLine.Option(names = "-gp", required = false, description = "cluster gossip port")
    int gossipPort = 12000;

    @CommandLine.Option(names = "-ws", required = false, description = "wait for seed")
    boolean waitForSeed = false;

    @CommandLine.Option(names = "-sn", required = false, description = "seed dns name")
    String seedName = null;

    @CommandLine.Option(names = "-sp", required = false, description = "service port")
    int servicePort = 10000;

    @CommandLine.Option(names = "-sd", required = false, description = "seeds nodes")
    List<String> seeds;

    List<Address> endpoints = Collections.EMPTY_LIST;

    public AppConfig() {
    }

    public AppConfig(Builder builder) {
        this.gossipPort = builder.gossipPort;
        this.seedName = builder.seedName;
        this.servicePort = builder.servicePort;
        this.waitForSeed = builder.waitForSeed;
        this.seeds = new LinkedList<>(builder.seeds);
    }

    @Override
    public Integer call() throws Exception {
        if (seeds == null) {
            return 0;
        }
        if (waitForSeed && seedName == null) {
            throw new CloudException("seed name is missing");
        }

        endpoints = seeds.stream().map(s -> s.split(":")).map(arr -> Address.create(arr[0], Integer.valueOf(arr[1]))).collect(Collectors.toList());
        return endpoints.size();
    }

    public int getGossipPort() {
        return gossipPort;
    }

    public Address[] getEndpoints() {
        return endpoints.toArray(Address[]::new);
    }

    public int getServicePort() {
        return servicePort;
    }

    public Mono<NodeConfig.Builder> getNodeConfigBuilder() {
        NodeConfig.Builder builder = NodeConfig.builder();
        if (servicePort > 0) {
            builder.withNodePort(servicePort);
        }
        if (gossipPort > 0) {
            builder.withGossipPort(gossipPort);
        }
        endpoints = seeds.stream().map(s -> s.split(":")).map(arr -> Address.create(arr[0], Integer.valueOf(arr[1]))).collect(Collectors.toList());
        if (endpoints.size() > 0) {
            builder.withSeedNodes(getEndpoints());
        }
        if(seedName != null)
        {
            this.waitForSeed = true;
        }
        if (!waitForSeed) {
            return Mono.just(builder);
        } else {
            return Mono.fromSupplier(() -> resolveDns(seedName))
                    .doOnError(thr -> System.out.println(thr.getMessage()))
                    .retryWhen(RetryUtil.wrap(Retry
                            .any()
                            .exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofSeconds(20)))
                    )
                    .doOnNext(s -> System.out.println("seed node found : " + s))
                    .map(s -> Address.create(s, gossipPort))
                    .doOnNext(addr -> System.out.println("seed addr : " + addr))
                    .doOnNext(addr -> builder.withSeedNodes(addr))
                    .map(s -> builder);
        }

    }

    private String resolveDns(String serviceName) {
        String addr = serviceName;//StringLookupFactory.INSTANCE.dnsStringLookup().lookup("address|" + seedName);
        if (addr == null) {
            throw new CloudException("service addr is null");
        }
        return addr;
    }

    public static Builder builder()
    {
        return new Builder();
    }
    
    public static final class Builder {

        int gossipPort = 12000;

        boolean waitForSeed = false;

        String seedName = null;

        int servicePort = 10000;

        List<String> seeds = new LinkedList<>();

        public Builder withGossipPort(int port) {
            this.gossipPort = port;
            return this;
        }

        public Builder withSeedName(String seedName) {
            this.seedName = seedName;
            this.waitForSeed = seedName != null;
            return this;
        }

        public Builder withServicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }


        public Builder withSeeds(String... seeds) {
            this.seeds.addAll(Arrays.asList(seeds));
            return this;
        }

        public AppConfig build() {
            return new AppConfig(this);
        }
    }
}
