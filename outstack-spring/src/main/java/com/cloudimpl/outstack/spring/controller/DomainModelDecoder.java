/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.controller;

import com.cloudimpl.outstack.spring.controller.exception.BadRequestException;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class DomainModelDecoder {
    
    public static String DOMAIN_MODEL_PREFIX = "domain-model=";
    
    public static Optional<String> decode(String contentType)
    {
        int index = contentType.indexOf(DOMAIN_MODEL_PREFIX);
        if(index == -1)
            return Optional.empty();
        String cmd =  contentType.substring(index + DOMAIN_MODEL_PREFIX.length());
        return Optional.of(cmd);
    }
}
