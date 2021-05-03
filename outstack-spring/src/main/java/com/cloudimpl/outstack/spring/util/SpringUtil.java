/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.util;

import com.cloudimpl.outstack.app.ServiceMeta;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author nuwan
 */
public class SpringUtil {

    public static ServiceMeta serviceProviderMeta(Class<? extends ServiceProvider> funcType) {
        CloudFunction func = funcType.getAnnotation(CloudFunction.class);
        Objects.requireNonNull(func);
        Router router = funcType.getAnnotation(Router.class);
        Objects.requireNonNull(func);
        Class<? extends RootEntity> rootType = Util.extractGenericParameter(funcType,ServiceProvider.class, 0);
        EntityMeta entityMeta = rootType.getAnnotation(EntityMeta.class);
        Map<String,String> attr = new HashMap<>();
        attr.put("srvType","serviceProvider");
        attr.put("rootType", rootType.getSimpleName());
        attr.put("isTenant", String.valueOf(RootEntity.isMyType(rootType)));
        //attr.put("plural", value)
        return new ServiceMeta(funcType, func, router,attr);
    }
}
