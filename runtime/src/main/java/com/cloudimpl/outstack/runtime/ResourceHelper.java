/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.IResource;
import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public class ResourceHelper {
    private final String resourceOwner;
    private final String resourceContext;

    public ResourceHelper(String resourceOwner, String resourceContext) {
        this.resourceOwner = resourceOwner;
        this.resourceContext = resourceContext;
    }
    
    public  String getFQBrn(IResource resource)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", resourceOwner,resourceContext,resource.getBRN());
    }
    
    public  String getFQTrn(IResource resource)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", resourceOwner,resourceContext,resource.getTRN());
    }
    
    public  String getFQBrn(String rn)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", resourceOwner,resourceContext,rn);
    }
    
    public  String getFQTrn(String trn)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", resourceOwner,resourceContext,trn);
    }
    
    @Override
    public String toString()
    {
        return MessageFormat.format("{0}:{1}",resourceOwner, resourceContext);
    }
}
