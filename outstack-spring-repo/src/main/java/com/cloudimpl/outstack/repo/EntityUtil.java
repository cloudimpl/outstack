package com.cloudimpl.outstack.repo;

public class EntityUtil {

    public static <T extends Entity> T with(T entity,String tid,String tenantId, long createdTime,long updatedTime)
    {
        return entity.withTid(tid).getMeta().withCreatedTime(createdTime).withUpdatedTime(updatedTime).entity();
    }

    public static <T extends Entity> T withTid(T entity,String tid)
    {
        return entity.withTid(tid);
    }

    public static Entity.Meta withCreatedTime(Entity.Meta meta, long time)
    {
        return meta.withCreatedTime(time);
    }

    public static Entity.Meta withUpdatedTime(Entity.Meta meta, long time)
    {
        return meta.withUpdatedTime(time);
    }

    public static Entity.Meta withTenantId(Entity.Meta meta,String tenantId)
    {
        return meta.withTenantId(tenantId);
    }
}
