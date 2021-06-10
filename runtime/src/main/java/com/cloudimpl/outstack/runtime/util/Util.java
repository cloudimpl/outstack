/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.util;

import com.cloudimpl.outstack.collection.error.CollectionException;
import com.cloudimpl.outstack.runtime.domainspec.Id;
import com.cloudimpl.outstack.runtime.domainspec.IgnoreCase;
import com.cloudimpl.outstack.runtime.ReflectionException;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import reactor.util.retry.Retry;

/**
 *
 * @author nuwansa
 */
public class Util {

    public static <T> T createObject(Class<T> type, VarArg<Class<?>> types, VarArg<Object> args) {
        try {
            return type.getConstructor(types.getArgs()).newInstance(args.getArgs());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
             throw new ReflectionException(ex);
        }
    }

    public static boolean isIdFieldIgnoreCase(Class<? extends Entity> entity) {
        Field f = getIdField(entity);
        return f.isAnnotationPresent(IgnoreCase.class);
    }

    public static Field getIdField(Class<? extends Entity> type)
    {
        return Arrays.asList(type.getDeclaredFields()).stream().filter(e -> e.isAnnotationPresent(Id.class)).findFirst().orElseThrow(()->new RuntimeException("Id Field not found in :"+type));
    }
    
    public static Retry wrap(reactor.retry.Retry retry) {
        return Retry.withThrowable(retry);
    }

    public static <T> Class<T> classForName(String name) {
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException ex) {
            throw new ReflectionException(ex);
        }
    }

    public static <T> Class<T> extractGenericParameter(final Class<?> parameterizedSubClass, final Class<?> genericSuperClass, final int pos) {

        Map<TypeVariable<?>, Class<?>> mapping = new HashMap<>();

        Class<?> klass = parameterizedSubClass;
        while (klass != null) {
            Type superType = klass.getGenericSuperclass();
            Type[] genericInterfaces = klass.getGenericInterfaces();
            for (int j = 0; j <= genericInterfaces.length; ++j) {
                Type type = (j == 0) ? superType : genericInterfaces[j - 1];
                if (type instanceof ParameterizedType) {
                    ParameterizedType parType = (ParameterizedType) type;
                    Type rawType = parType.getRawType();
                    if (rawType.equals(genericSuperClass)) {
                        // found
                        Type t = parType.getActualTypeArguments()[pos];
                        if (t instanceof Class<?>) {
                            return (Class<T>) t;
                        } else {
                            return (Class<T>) mapping.get((TypeVariable<?>) t);
                        }
                    }
                    // resolve
                    Type[] vars = ((GenericDeclaration) (parType.getRawType())).getTypeParameters();
                    Type[] args = parType.getActualTypeArguments();
                    for (int i = 0; i < vars.length; i++) {
                        if (args[i] instanceof Class<?>) {
                            mapping.put((TypeVariable) vars[i], (Class<?>) args[i]);
                        } else {
                            mapping.put((TypeVariable) vars[i], mapping.get((TypeVariable<?>) (args[i])));
                        }
                    }
                    klass = (Class<?>) rawType;
                } else {
                    klass = klass.getSuperclass();
                }
            }
        }
        throw CollectionException.RELECTION_EXCEPTION(err -> err.wrap(new RuntimeException("template parameter not found")));
    }

//    public static ResourceDescriptor parserResource(String resourceName)
//    {
//        
//        //Organization/1234/Child/234
//        //Tenant/1234/User/32515
//    }
    public static final class VarArg<T> {

        private final T[] args;

        public VarArg(T... args) {
            this.args = args;
        }

        public T[] getArgs() {
            return args;
        }

    }
}
