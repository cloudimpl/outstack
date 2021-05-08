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

import com.cloudimpl.outstack.collection.AwsCollectionProvider;
import com.cloudimpl.outstack.collection.CollectionOptions;
import com.cloudimpl.outstack.collection.CollectionProvider;
import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.CloudMessageDecoder;
import com.cloudimpl.outstack.common.CloudMessageEncoder;
import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.core.Injector;
import com.cloudimpl.outstack.logger.ConsoleLogWriter;
import com.cloudimpl.outstack.logger.LogWriter;
import com.cloudimpl.outstack.node.CloudNode;
import picocli.CommandLine;

/**
 *
 * @author nuwansa
 */
public class CloudApp {
     static{
         GsonCodec.registerTypeAdaptor(CloudMessage.class, () -> new CloudMessageDecoder(), () -> new CloudMessageEncoder());
    }
    public static void main(String[] args) throws InterruptedException {
        AppConfig appConfig = new AppConfig();
        new CommandLine(appConfig).execute(args);
        Injector injector = new Injector();
        injector.bind(LogWriter.class).to(new ConsoleLogWriter());
        injector.bind(CollectionProvider.class).to(new AwsCollectionProvider("http://localhost:4566"));
        injector.nameBind("leaderOptions",CollectionOptions.builder().withOption("TableName", "Test").build());
        ResourcesLoader serviceLoader = new ResourcesLoader();
        serviceLoader.preload();
        appConfig.getNodeConfigBuilder().doOnNext(c->c.withServiceEndpoints(serviceLoader.getEndpoints())).map(C->C.build())
                .doOnNext(c -> {
            CloudNode node = new CloudNode(injector, c);
            serviceLoader.init(node);
            node.start();
        }).subscribe();

        Thread.currentThread().join();
    }

}
