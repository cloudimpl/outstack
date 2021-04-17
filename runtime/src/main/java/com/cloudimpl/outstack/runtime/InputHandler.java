/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import java.util.function.Function;

/**
 *
 * @author nuwan
 * @param <T>
 * @param <R>
 */
public interface InputHandler<T extends Input,R> extends Function<T, R>{

}
