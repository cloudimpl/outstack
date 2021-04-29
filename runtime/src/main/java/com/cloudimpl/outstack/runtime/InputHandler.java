/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Input;
import java.util.function.BiFunction;

/**
 *
 * @author nuwan
 * @param <C>
 * @param <T>
 * @param <R>
 */
public interface InputHandler<C extends Context,T extends Input,R> extends BiFunction<C,T, R>{

}
