/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.app.ResourcesLoader;
import com.cloudimpl.outstack.app.ServiceMeta;
import com.cloudimpl.outstack.spring.util.SpringUtil;
import com.cloudimpl.outstack.util.SrvUtil;

/**
 *
 * @author nuwan
 */
public class ResourcesLoaderEx extends ResourcesLoader{
    
    @Override
    protected ServiceMeta toServiceMeta(Class<?> serviceType)
    {
        if(SpringService.class.isAssignableFrom(serviceType))
        {
            return SpringUtil.serviceProviderMeta((Class<? extends SpringService>) serviceType);
        }
        else if(SpringQueryService.class.isAssignableFrom(serviceType))
        {
            return SpringUtil.serviceQueryProviderMeta((Class<? extends SpringQueryService>) serviceType);
        }
        return SrvUtil.serviceMeta(serviceType);
    }
}
