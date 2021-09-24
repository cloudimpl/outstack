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

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nuwan
 * @param <R>
 */
public interface ITransaction<R extends RootEntity> extends QueryOperations<R> {

    List<Event> getEventList();

    Collection<Entity> getEntityList();

    Map<String, Entity> getDeletedEntities();

    Map<String, Entity> getRenameEntities();

    void setAttachment(Object attachment);

    <K> K getAttachment();

    default boolean isEntityRenamed(String trn) {
        return getRenameEntities().containsKey(trn);
    }

    default Collection<ITransaction<R>> getTxList() {
        return Collections.singletonList(this);
    }

    <K> K getReply();

    <C extends ChildEntity<R>, K extends Entity, Z extends EntityQueryContext> Z getContext(Class<K> entityType);

    InputMetaProvider getInputMetaProvider();

    Class<R> getRootType();
}
