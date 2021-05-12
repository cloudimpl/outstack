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
    private final String domainOwner;
    private final String domainContext;
    private final String apiContext;
    public ResourceHelper(String domainOwner, String domainContext, String apiContext) {
        this.domainOwner = domainOwner;
        this.domainContext = domainContext;
        this.apiContext = apiContext;
    }
    
    public  String getFQBrn(IResource resource)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", domainOwner,domainContext,resource.getBRN());
    }
    
    public  String getFQTrn(IResource resource)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", domainOwner,domainContext,resource.getTRN());
    }
    
    public  String getFQBrn(String rn)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", domainOwner,domainContext,rn);
    }
    
    public  String getFQTrn(String trn)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", domainOwner,domainContext,trn);
    }

    public String getDomainContext() {
        return domainContext;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getApiContext() {
        return apiContext;
    }
    
    
    @Override
    public String toString()
    {
        return MessageFormat.format("{0}:{1}",domainOwner, domainContext);
    }
}
