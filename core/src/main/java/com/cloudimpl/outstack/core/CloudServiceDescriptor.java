/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.core;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.GsonCodec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwansa
 */
public class CloudServiceDescriptor {

    private final String serviceId;
    private final String name;
    private final String functionType;
    private final String inputType;
    private final CloudRouterDescriptor routerDescriptor;
    private final String hostAddr;
    private final int servicePort;
    private final Map<String, String> attr;

    public CloudServiceDescriptor(Builder builder) {
        this.serviceId = builder.serviceId;
        this.name = builder.name;
        this.functionType = builder.functionType;
        this.inputType = builder.inputType;
        this.routerDescriptor = builder.routerDescriptor;
        this.hostAddr = builder.hostAddr;
        this.servicePort = builder.servicePort;
        this.attr = Collections.unmodifiableMap(builder.attr);
    }

    public String getName() {
        return name;
    }

    public Class<? extends Function<CloudMessage, Publisher>> getFunctionType() {
        return CloudUtil.classForName(functionType);
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public String getInputType() {
        return inputType;
    }

    public CloudRouterDescriptor getRouterDescriptor() {
        return routerDescriptor;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getHostAddr() {
        return hostAddr;
    }

    public int getServicePort() {
        return servicePort;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return GsonCodec.encode(this);
    }

    public static final class Builder {

        private String serviceId;
        private String name;
        private String functionType;
        private String inputType;
        private CloudRouterDescriptor routerDescriptor;
        private String hostAddr;
        private int servicePort;
        private Map<String,String> attr = new HashMap<>();
        
        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder withAttr(Map<String,String> attr)
        {
            this.attr = attr;
            return this;
        }
        
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withFunctionType(String functionType) {
            this.functionType = functionType;
            return this;
        }

        public Builder withInputType(String inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder withRouterDescriptor(CloudRouterDescriptor routerDesc) {
            this.routerDescriptor = routerDesc;
            return this;
        }

        public Builder withHostAddress(String hostAddress) {
            this.hostAddr = hostAddress;
            return this;
        }

        public Builder withServicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public CloudServiceDescriptor build() {
            return new CloudServiceDescriptor(this);
        }
    }
}
