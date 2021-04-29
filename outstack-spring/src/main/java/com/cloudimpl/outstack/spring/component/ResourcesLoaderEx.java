/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.app.ResourcesLoader;
import com.cloudimpl.outstack.app.ServiceMeta;
import com.cloudimpl.outstack.runtime.ServiceProvider;
import com.cloudimpl.outstack.util.SrvUtil;

/**
 *
 * @author nuwan
 */
public class ResourcesLoaderEx extends ResourcesLoader{
    
    @Override
    protected ServiceMeta toServiceMeta(Class<?> serviceType)
    {
        if(ServiceProvider.class.isAssignableFrom(serviceType))
        {
            
        }
        return SrvUtil.serviceMeta(serviceType);
    }
}
