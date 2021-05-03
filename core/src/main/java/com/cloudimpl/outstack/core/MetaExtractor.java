/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.core;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public interface MetaExtractor {  
    Map<String,String> extract(Class<?> serviceType);
    
    public static class EmptyExtractor implements MetaExtractor
    {
        @Override
        public Map<String, String> extract(Class<?> serviceType) {
            return Collections.EMPTY_MAP;
        }   
    }
}
