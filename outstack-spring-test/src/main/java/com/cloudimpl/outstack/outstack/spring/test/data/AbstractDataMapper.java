package com.cloudimpl.outstack.outstack.spring.test.data;

import com.cloudimpl.outstack.outstack.spring.test.exception.TestRuntimeException;
import com.google.common.base.Defaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractDataMapper<T> {

    private final Class<T> entityType;
    private Map<String, Field> fieldMap;

    public AbstractDataMapper(Class<T> entityType) {
        this.entityType = entityType;
        this.fieldMap = new HashMap<>();
        scanFields(entityType);
    }

    private void scanFields(Class<T> entityType) {

        this.fieldMap = Arrays.stream(entityType.getDeclaredFields())
                .peek(e -> e.setAccessible(true))
                .collect(Collectors.toMap(Field::getName, Function.identity()));
    }

    protected T getNewInstance() {
        Constructor<T> constructor = (Constructor<T>) entityType.getConstructors()[0];
        List<?> parameters = Arrays.stream(constructor.getParameterTypes())
                .map(Defaults::defaultValue)
                .collect(Collectors.toList());
        try {
            return constructor.newInstance(parameters.toArray());
        } catch (Exception e) {
            log.debug("Cannot invoke the constructor");
            throw TestRuntimeException.getInstantiationFailureException(entityType);
        }
    }

    protected void setValue(T targetObject, String fieldName, Object value) {
        if (!CollectionUtils.isEmpty(fieldMap) && fieldMap.containsKey(fieldName)) {
            Field field = fieldMap.get(fieldName);
            try {
                field.set(targetObject, field.getType().cast(value));
            } catch (IllegalAccessException e) {
                log.debug("Could not set field", e);
                throw TestRuntimeException.getCannotSetValueToTheFieldException(field);
            }
        }
    }

    public Class<T> getEntityType() {
        return entityType;
    }
}
