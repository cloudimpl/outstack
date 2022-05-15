package com.cloudimpl.outstack.repo;

import org.springframework.data.annotation.Id;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;

public class RepoUtil {

    public static String createUUID()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("id-");
        return builder.append(UUID.randomUUID())
                .toString();
    }


    public static Table getRepoMeta(Class<?> repoType,boolean validate)
    {
        Table table = repoType.getAnnotation(Table.class);
        if(table == null && validate)
        {
            throw new RepoException("no table annotation defined in the repository "+repoType.getName());
        }
        return table;
    }

    public static Field getIdField(Class<? extends Entity> type) {
        Field f =  (Field) Arrays.asList(type.getDeclaredFields()).stream().filter((e) -> {
            return e.isAnnotationPresent(Id.class);
        }).findFirst().orElseThrow(() -> {
            return new RuntimeException("Id Field not found in :" + type);
        });
        f.setAccessible(true);
        return f;
    }

    public static <T> T getValue(Object value, Field field)
    {
        try {
            return (T)field.get(value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
