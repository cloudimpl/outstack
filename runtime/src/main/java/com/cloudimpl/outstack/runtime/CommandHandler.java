/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Entity;

/**
 *
 * @author nuwan
 * @param <T>
 */
public interface CommandHandler<T extends Entity> extends Handler<T>{
    
}
