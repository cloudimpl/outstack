package com.cloudimpl.outstack.repo;

public class Event {
    private String eventId;
    private Event.Meta meta = new Event.Meta();

    protected <T extends Event> T withEventId(String eventId)
    {
        this.eventId = eventId;
        return (T)this;
    }

    public Meta getMeta()
    {
        if(this.meta == null)
        {
            this.meta = new Meta();
        }
        return this.meta;
    }

    public String getEventId(){
        return this.eventId;
    }

    public  final class Meta
    {
        private String tenantId;
        private long createdTime;
        private long updatedTime;

        protected Event.Meta withTenantId(String tenantId)
        {
            if(tenantId != null && tenantId.equals("default"))
            {
                this.tenantId = null;
            }else
            {
                this.tenantId = tenantId;
            }
            return this;
        }

        protected Event.Meta withCreatedTime(long createdTime)
        {
            this.createdTime = createdTime;
            return this;
        }

        protected Event.Meta withUpdatedTime(long updatedTime)
        {
            this.updatedTime = updatedTime;
            return this;
        }

        public String getTenantId()
        {
            return tenantId;
        }

        public long getCreatedTime()
        {
            return this.createdTime;
        }

        public long getUpdatedTime()
        {
            return this.updatedTime;
        }


        public <T extends Event> T  entity()
        {
            return (T) Event.this;
        }

    }
}
