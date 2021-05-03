/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Command;
import java.util.function.BiFunction;

/**
 *
 * @author nuwansa
 * @param <C>
 * @param <T>
 */
public interface CommandHandler<C extends Context,T extends Command, R> extends BiFunction<C, T, R>{

}
