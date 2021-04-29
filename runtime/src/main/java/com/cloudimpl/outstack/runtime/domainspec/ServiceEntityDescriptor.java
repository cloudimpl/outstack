/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.domainspec;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public class ServiceEntityDescriptor {
    private final String rootType;
    private final String plural;
    private final Map<String,String> childMappers;

    public ServiceEntityDescriptor(String rootType, String plural) {
        this.childMappers = new HashMap<>();
        this.rootType = rootType;
        this.plural = plural;
    }
    
    public void putType(String plural,String type)
    {
        this.childMappers.put(plural, type);
    }
}
