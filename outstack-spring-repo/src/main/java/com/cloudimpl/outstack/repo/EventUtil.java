package com.cloudimpl.outstack.repo;

public class EventUtil {

    public static <T extends Event> T with(T event,String tenantId,String eventId, long createdTime,long updatedTime)
    {
        event.withEventId(eventId).getMeta().withTenantId(tenantId).withCreatedTime(createdTime).withUpdatedTime(updatedTime);
        return event;
    }

    public static <T extends Event> T withEventId(T entity,String eventId)
    {
        return entity.withEventId(eventId);
    }

    public static  <T extends Event> T withCreatedTime(T event, long time)
    {
         return event.getMeta().withCreatedTime(time).entity();
    }

    public static <T extends Event> T withUpdatedTime(T event, long time)
    {
        return event.getMeta().withUpdatedTime(time).entity();
    }

    public static <T extends Event> T withTenantId(T event,String tenantId)
    {
        return event.getMeta().withTenantId(tenantId).entity();
    }
}
