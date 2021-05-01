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
    private final String productOwner;
    private final String product;

    public ResourceHelper(String productOwner, String product) {
        this.productOwner = productOwner;
        this.product = product;
    }
    
    public  String getFQBrn(IResource resource)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", productOwner,product,resource.getBRN());
    }
    
    public  String getFQTrn(IResource resource)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", productOwner,product,resource.getTRN());
    }
    
    public  String getFQBrn(String rn)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", productOwner,product,rn);
    }
    
    public  String getFQTrn(String trn)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", productOwner,product,trn);
    }
}
