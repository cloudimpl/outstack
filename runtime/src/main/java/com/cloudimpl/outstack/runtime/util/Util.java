/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.util;

import com.cloudimpl.outstack.collection.error.CollectionException;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author nuwansa
 */
public class Util {

    public static <T> T createObject(Class<T> type, VarArg<Class<?>> types, VarArg<Object> args) {
        try {
            return type.getConstructor(types.getArgs()).newInstance(args.getArgs());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw CollectionException.RELECTION_EXCEPTION(err->{});
        }
    }
    
    
    public static final class VarArg<T>
    {
        private final T[] args;

        public VarArg(T... args) {
            this.args = args;
        }

        public T[] getArgs() {
            return args;
        }
        
        
    }
}
