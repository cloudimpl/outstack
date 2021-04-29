/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.service;

import com.cloudimpl.outstack.common.CloudMessage;
import com.cloudimpl.outstack.common.RouterType;
import com.cloudimpl.outstack.core.annon.CloudFunction;
import com.cloudimpl.outstack.core.annon.Router;
import java.util.function.Function;

/**
 *
 * @author nuwan
 */
@CloudFunction(name = "RestController")
@Router(routerType = RouterType.LOCAL)
public class RestControllerService implements Function<CloudMessage, CloudMessage>{

    
    @Override
    public CloudMessage apply(CloudMessage req) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
