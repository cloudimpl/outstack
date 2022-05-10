package com.cloudimpl.outstack.repo;


import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

public abstract class Entity {
    private String tid;
    private String parentTid;
    private transient Field idField;
    private Meta meta;
    public Entity(){

        this.idField  = RepoUtil.getIdField(this.getClass());
        this.meta = new Meta();
    }

    public String getTid()
    {
        if(tid == null)
        {
            throw new RepoException("tid is null");
        }
        return tid;
    }

    public Meta getMeta()
    {
        if(meta == null)
        {
            meta = new Meta();
        }
        return meta;
    }

    public String id()
    {
        return RepoUtil.getValue(this,idField);
    }

    public Optional<String> getParentTid(){
        return Optional.ofNullable(parentTid);
    }

    protected <T extends Entity> T withTid(String tid)
    {
        this.tid = tid;
        return (T)this;
    }

    protected <T extends Entity> T setParentTid(String tid)
    {
        this.tid = tid;
        return (T)this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return tid.equals(entity.tid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid);
    }

    public  final class Meta
    {
        private String tenantId;
        private long createdTime;
        private long updatedTime;

        private Meta()
        {

        }

        protected Meta withTenantId(String tenantId)
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

        protected Meta withCreatedTime(long createdTime)
        {
            this.createdTime = createdTime;
            return this;
        }

        protected Meta withUpdatedTime(long updatedTime)
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


        public <T extends Entity> T  entity()
        {
            return (T) Entity.this;
        }

    }
}
