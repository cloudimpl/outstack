package com.cloudimpl.outstack.outstack.spring.test.data;

import com.cloudimpl.outstack.runtime.domainspec.Entity;

public class MockDataMapper<T extends Entity> extends AbstractDataMapper<T> {

    public MockDataMapper(Class<T> entityType) {
        super(entityType);
    }

    public T getMockInstance() {
        return getNewInstance();
    }
}
