package com.cloudimpl.outstack.outstack.spring.test.data;

import com.cloudimpl.outstack.outstack.spring.test.exception.TestRuntimeException;
import com.google.common.base.Defaults;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityMapper<T, K> {

    private final Class<T> sourceType;
    private final Class<K> targetType;
    private final Map<String, Field> sourceEntityFieldMap;
    private final Map<String, Field> targetEntityFieldMap;

    public EntityMapper(Class<T> sourceType, Class<K> targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.sourceEntityFieldMap = scanFields(sourceType);
        this.targetEntityFieldMap = scanFields(targetType);
    }

    private Map<String, Field> scanFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .peek(e -> e.setAccessible(true))
                .collect(Collectors.toMap(Field::getName, Function.identity()));
    }

    protected <V> V getNewInstance(Class<V> entityType) {
        Constructor<V> constructor = (Constructor<V>) entityType.getConstructors()[0];
        List<?> parameters = Arrays.stream(constructor.getParameterTypes())
                .map(Defaults::defaultValue)
                .collect(Collectors.toList());
        try {
            return constructor.newInstance(parameters.toArray());
        } catch (Exception e) {
            throw TestRuntimeException.getInstantiationFailureException(entityType);
        }
    }

    public void setValue(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw TestRuntimeException.getCannotSetValueToTheFieldException(field);
        }
    }

    public Object getValue(Object object, Field field) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw TestRuntimeException.getCannotGetValueFromTheFieldException(field);
        }
    }

    public K map(T source) {
        K targetInstance = getNewInstance(targetType);
        for (Map.Entry<String, Field> entry: sourceEntityFieldMap.entrySet()) {
            if (targetEntityFieldMap.containsKey(entry.getKey())) {
                setValue(targetInstance, targetEntityFieldMap.get(entry.getKey()), getValue(source, entry.getValue()));
            }
        }
        return targetInstance;
    }

    public Class<T> getSourceType() {
        return sourceType;
    }

    public Class<K> getTargetType() {
        return targetType;
    }
}
