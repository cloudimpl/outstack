package com.cloudimpl.outstack.outstack.spring.test.data;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableDataMapper<T extends Entity> extends AbstractDataMapper<T> {

    public TableDataMapper(Class<T> targetType) {
        super(targetType);
    }

    public T mapData(Map<String, String> row) {
        T newInstance = getNewInstance();
        if (!CollectionUtils.isEmpty(row)) {
            for (Map.Entry<String, String> entry: row.entrySet()) {
                setValue(newInstance, entry.getKey(), entry.getValue());
            }
        }
        return newInstance;
    }

    public List<T> mapData(List<Map<String, String>> rows) {
        if (!CollectionUtils.isEmpty(rows)) {
            return rows.stream().map(this::mapData).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
