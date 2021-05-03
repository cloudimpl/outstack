/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.outstack.spring.test;

import com.cloudimpl.outstack.runtime.Handler;
import io.cucumber.java.en.Given;

/**
 *
 * @author nuwan
 */
public class SpringServiceHandlerTestDriver {
    
    public static ResourceLoader resourceLoader = new ResourceLoader();
    
    private Handler handlers;
    
    @Given("testing {word} functionality of {word}")
    public void initHandler(String handlerName,String entity)
    {
        System.out.println("handler :"+handlerName+ " for entity : "+entity);
    }
}
