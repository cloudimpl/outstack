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
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import java.time.Duration;

/**
 *
 * @author nuwan
 */
public class EntityMetaDetailCache {

    private static final EntityMetaDetailCache instance = new EntityMetaDetailCache();

    private final ResourceCache<EntityMetaDetail> metaCache;

    public EntityMetaDetailCache() {
        metaCache = new ResourceCache<>(1000, Duration.ofSeconds(60));
    }

    public EntityMetaDetail getEntityMeta(Class<? extends Entity> entityType) {
        return metaCache.get(entityType.getName(), EntityMetaDetail.createMeta(entityType));
    }

    public static EntityMetaDetailCache instance() {
        return instance;
    }
}
