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
package com.cloudimpl.outstack.runtime.repo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author nuwan
 */
public class RepoStreamingReq {

    private final Map<String,List<ResourceInfo>> mapResources;
    private final List<ResourceInfo> initialDownload;
    
    public RepoStreamingReq(List<ResourceInfo> initialDownload,List<ResourceInfo> resourceList) {
        mapResources = resourceList.stream().collect(Collectors.groupingBy(r->r.getEntityType(), Collectors.toList()));
        this.initialDownload = new LinkedList<>(initialDownload);
    }

    public Optional<List<ResourceInfo>> getResources(String entityType)
    {
        return Optional.ofNullable(mapResources.get(entityType));
    }
    
    public List<ResourceInfo> getInitialDownloadResources()
    {
        return this.initialDownload;
    }
    
    public static final class ResourceInfo
    {
        private final String entityType;
        private final String entityId;
        private final String tenantId;

        public ResourceInfo(String entityType, String entityId, String tenantId) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.tenantId = tenantId;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getTenantId() {
            return tenantId;
        }
        
        
    }
}