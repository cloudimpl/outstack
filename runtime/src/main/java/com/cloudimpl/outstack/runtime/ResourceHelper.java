/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domain.v1.IResource;
import java.text.MessageFormat;

/**
 *
 * @author nuwan
 */
public class ResourceHelper {
    public static String getBrn(String organization,String product,IResource resource)
    {
        return MessageFormat.format("brn:{0}:{1}:{2}", organization,product,resource.getRN());
    }
    
    public static String getTrn(String organization,String product,IResource resource)
    {
        return MessageFormat.format("trn:{0}:{1}:{2}", organization,product,resource.getTRN());
    }
}
