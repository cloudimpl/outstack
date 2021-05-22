package com.cloudimpl.outstack.outstack.spring.test.data;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import org.apache.logging.log4j.util.Strings;

public class EntityUtil<T> extends AbstractDataMapper<T> {

    public EntityUtil(Class<T> entityType) {
        super(entityType);
    }

    public static <K> K getInstance(Class<K> type) {
        return new EntityUtil<>(type).getNewInstance();
    }

    public static <K extends Entity> String getVersion(Class<K> type) {
        if (type.isAnnotationPresent(EntityMeta.class)) {
            return type.getAnnotation(EntityMeta.class).version();
        }
        return Strings.EMPTY;
    }
}
