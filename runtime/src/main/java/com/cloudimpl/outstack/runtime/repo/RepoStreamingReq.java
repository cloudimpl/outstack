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

    private final Map<String, List<ResourceInfo>> mapResources;
    private final List<ResourceInfo> initialDownload;

    public RepoStreamingReq(List<ResourceInfo> initialDownload, List<ResourceInfo> resourceList) {
        mapResources = resourceList.stream().collect(Collectors.groupingBy(r -> r.getEntityType(), Collectors.toList()));
        this.initialDownload = new LinkedList<>(initialDownload);
    }

    public Optional<List<ResourceInfo>> getResources(String entityType) {
        return Optional.ofNullable(mapResources.get(entityType));
    }

    public List<ResourceInfo> getInitialDownloadResources() {
        return this.initialDownload;
    }

    public static final class ResourceInfo {

        private final String entityType;
        private final String entityId;
        private final String tenantId;
        private final String childType;
        private final String childId;
        private final String searchParam;
        public ResourceInfo(String entityType, String entityId, String tenantId) {
            this(entityType, entityId, null, null, tenantId);
        }

        public ResourceInfo(String entityType, String entityId, String tenantId, String searchParam) {
            this(entityType, entityId, null, null, tenantId, searchParam);
        }

        public ResourceInfo(String entityType, String entityId, String childType, String childId, String tenantId) {
            this(entityType, entityId, childType, childId, tenantId, null);
        }

        public ResourceInfo(String entityType, String entityId, String childType, String childId, String tenantId, String searchParam) {
            this.entityType = entityType;
            this.entityId = entityId;
            this.childType = childType;
            this.childId = childId;
            this.tenantId = tenantId;
            this.searchParam = searchParam;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getSearchParam() {
            return searchParam;
        }

        public String getEntityType() {
            return entityType;
        }

        public String getTenantId() {
            return tenantId;
        }

        public String getChildType() {
            return childType;
        }

        public String getChildId() {
            return childId;
        }
    }
}
