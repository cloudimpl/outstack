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


import com.cloudimpl.outstack.core.CloudException;
import com.cloudimpl.outstack.core.CloudRouterDescriptor;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.coreImpl.ServiceEndpointPlugin;
import com.cloudimpl.outstack.node.CloudNode;
import com.cloudimpl.outstack.util.SrvUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwansa
 */
public class ResourcesLoader {

    private final List<ServiceMeta> metaList;
    private final List<Class<? extends ServiceEndpointPlugin>> endpoints;
    public ResourcesLoader() {
        ScanResult rs = new ClassGraph().enableAnnotationInfo().enableClassInfo().scan();
        ClassInfoList list = rs.getClassesWithAnnotation(CloudFunction.class.getName());
        metaList = list.loadClasses().stream().map(this::toServiceMeta).collect(Collectors.toList());
        list = rs.getClassesImplementing(ServiceEndpointPlugin.class.getName());
        endpoints = list.loadClasses().stream().map(s->s.asSubclass(ServiceEndpointPlugin.class)).collect(Collectors.toList());
    }
    
    protected ServiceMeta toServiceMeta(Class<?> serviceType)
    {
        return SrvUtil.serviceMeta(serviceType);
    }
    
    public List<ServiceMeta> getList()
    {
        return metaList;
    }
    
    public List<Class<? extends ServiceEndpointPlugin>> getEndpoints()
    {
        return endpoints;
    }
    
    public void init(CloudNode node)
    {
        getList().stream().forEach(s->registerService(s, node));
    }
    
    private void registerService(ServiceMeta meta,CloudNode node)
    {
        switch(meta.getRouter().routerType())
        {
            case ROUND_ROBIN:
            case SERVICE_ID:
            case LEADER:
            case LOCAL:
            case NODE_ID:
            {
                node.registerService(meta.getFunc().name(), com.cloudimpl.outstack.core.CloudFunction.builder()
                        .withFunction((Class<? extends Function<?, ? extends Publisher>>) meta.getServiceType())
                        .withId(meta.getFunc().id()).withAttr(meta.getAttr())
                .withRouter(CloudRouterDescriptor.builder().withRouterType(meta.getRouterType()).build()).build());
                break;
            }
            case DYNAMIC:
            {
                node.registerService(meta.getFunc().name(), com.cloudimpl.outstack.core.CloudFunction.builder()
                        .withFunction((Class<? extends Function<?, ? extends Publisher>>) meta.getServiceType())
                        .withId(meta.getFunc().id()).withAttr(meta.getAttr())
                .withRouter(CloudRouterDescriptor.builder()
                        .withRouterType(meta.getRouterType())
                        .withLoadBalancer(meta.getRouter()
                                .loadBalancer())
                        .build()).build());
                break;
            } 
            default:
                throw new CloudException("unhandled router type : "+meta.getRouter().routerType());
        }
    }
    
}
