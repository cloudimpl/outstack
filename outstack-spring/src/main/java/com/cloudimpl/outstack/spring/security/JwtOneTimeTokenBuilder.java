/*
 * Copyright 2021 nuwan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudimpl.outstack.spring.security;

import com.cloudimpl.outstack.common.GsonCodec;
import com.cloudimpl.outstack.runtime.EntityMetaDetail;
import com.cloudimpl.outstack.runtime.EntityMetaDetailCache;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;

/**
 *
 * @author nuwan
 */
public class JwtOneTimeTokenBuilder extends JwtTokenBuilder {

    public JwtOneTimeTokenBuilder withAction(String actionName) {
        this.withClaim("@action", actionName);
        return this;
    }

    public JwtOneTimeTokenBuilder withRootId(String rootId) {
        this.withClaim("@rootId", rootId);
        return this;
    }

    public JwtOneTimeTokenBuilder withChildId(String childId) {
        this.withClaim("@childId", childId);
        return this;
    }

    public JwtOneTimeTokenBuilder withActionInput(Object req) {
        this.withClaim("@input", GsonCodec.encode(req));
        return this;
    }

    public JwtOneTimeTokenBuilder withRootEntityType(Class<? extends RootEntity> rootType) {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(rootType);
        this.withClaim("@rootType", meta.getPlural());
        this.withClaim("@version", meta.getVersion());
        return this;
    }

    public JwtOneTimeTokenBuilder withChildEntityType(Class<? extends ChildEntity> childType) {
        EntityMetaDetail meta = EntityMetaDetailCache.instance().getEntityMeta(childType);
        this.withClaim("@childType", meta.getPlural());
        return this;
    }
}
