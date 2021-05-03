/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Input;
import java.util.function.BiConsumer;

/**
 *
 * @author nuwan
 * @param <C>
 * @param <I>
 */
public interface EventHandler <C extends Context,I extends Input> extends BiConsumer<C, I>,Handler{
    
}
