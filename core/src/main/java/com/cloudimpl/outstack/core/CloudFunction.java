/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose
 * Tools | Templates and open the template in the editor.
 */
package com.cloudimpl.outstack.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 *
 * @author nuwansa
 */
public class CloudFunction {

    private final String functionType;
    private final String inputType;
    private final String id;
    private final CloudRouterDescriptor routerDesc;
    private final Map<String,String> attr;
    public CloudFunction(String id, String functionType, String inputType, CloudRouterDescriptor routerDesc,Map<String,String> attr) {
        this.id = id;
        this.functionType = functionType;
        this.inputType = inputType;
        this.routerDesc = routerDesc;
        this.attr = Collections.unmodifiableMap(attr);
    }

    public String getFunctionType() {
        return functionType;
    }

    public String getInputType() {
        return inputType;
    }

    public String getId() {
        return id;
    }

    public CloudRouterDescriptor getRouterDesc() {
        return routerDesc;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Class<? extends Function> functionType;
        private String inputType;
        private CloudRouterDescriptor routerDesc;
        private String id;
        private Map<String,String> attr = Collections.EMPTY_MAP;
        public Builder withFunction(Class<? extends Function<?, ? extends Publisher>> functionType) {
            this.functionType = functionType;
            this.inputType = CloudUtil.extractGenericParameter(functionType, Function.class, 0).getName();
            return this;
        }

        public Builder withAttr(Map<String,String> attr)
        {
            this.attr = attr;
            return this;
        }
        
        public Builder withRouter(CloudRouterDescriptor routerDesc) {
            this.routerDesc = routerDesc;
            return this;
        }

        public Builder withId(String id){
            this.id = id;
            return this;
        }
        
        public CloudFunction build() {
            return new CloudFunction(this.id,functionType.getName(), inputType, routerDesc,attr);
        }
    }
}
