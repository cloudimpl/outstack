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
import com.cloudimpl.outstack.runtime.domainspec.EntityMeta;
import com.cloudimpl.outstack.runtime.util.Util;
import java.lang.reflect.Field;

/**
 *
 * @author nuwan
 */
public class EntityMetaDetail{
    private final Class<? extends Entity> type;
    private final boolean isIdIgnoreCase;
    private final Field idField;
    private final String version;

    public EntityMetaDetail(Class<? extends Entity> type, boolean isIdIgnoreCase, Field idField,String version) {
        this.type = type;
        this.isIdIgnoreCase = isIdIgnoreCase;
        this.idField = idField;
        this.version = version;
    }

    public Class<? extends Entity> getType() {
        return type;
    }

    public boolean isIdIgnoreCase() {
        return isIdIgnoreCase;
    }

    public Field getIdField() {
        return idField;
    }

    public String getVersion() {
        return version;
    }
   
    public static EntityMetaDetail createMeta(Class<? extends Entity> cls)
    {
        return new EntityMetaDetail(cls,Util.isIdFieldIgnoreCase(cls),Util.getIdField(cls),cls.getAnnotation(EntityMeta.class).version());
    }
}
