/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import com.cloudimpl.outstack.runtime.domainspec.TenantRequirement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author nuwan
 */
public class SpringServiceDescriptor {

    private final Map<String, ActionDescriptor> rootActions;
    private final Map<String, Map<String, ActionDescriptor>> childActions;
    private final Map<String, EntityDescriptor> mapDescriptors;
    private final EntityDescriptor rootDesc;
    private final String version;
    private final TenantRequirement tenancy;
    private final String serviceName;
    private final String domainOwner;
    private final String domainContext;
    private final String apiContext;

    public SpringServiceDescriptor(String apiContext, String domainOwner, String domainContext, String serviceName, String rootType, String version, String plural, TenantRequirement tenancy) {
        this.apiContext = apiContext;
        this.serviceName = serviceName;
        this.domainContext = domainContext;
        this.domainOwner = domainOwner;
        this.rootDesc = new EntityDescriptor(rootType, plural);
        this.version = version;
        this.tenancy = tenancy;
        this.rootActions = new HashMap<>();
        this.childActions = new HashMap<>();
        this.mapDescriptors = new HashMap<>();
    }

    public Collection<ActionDescriptor> getRootActions() {
        return rootActions.values();
    }

    public Collection<ActionDescriptor> getChildActions(String childEntity) {
        return childActions.getOrDefault(childEntity, Collections.EMPTY_MAP).values();
    }

    public Collection<EntityDescriptor> entityDescriptors() {
        return mapDescriptors.values();
    }

    public void putRootAction(ActionDescriptor action) {
        this.rootActions.put(action.getName(), action);
    }

    public String getApiContext() {
        return apiContext;
    }

    public void putChildAction(EntityDescriptor child, ActionDescriptor action) {
        this.mapDescriptors.put(child.getPlural().toLowerCase(), child);
        Map<String, ActionDescriptor> map = childActions.get(child.getName());
        if (map == null) {
            map = new HashMap<>();
            childActions.put(child.getName(), map);
        }
        map.put(action.getName(), action);
    }

    public TenantRequirement getTenancy() {
        return tenancy;
    }

    public Optional<EntityDescriptor> getEntityDescriptorByChildType(String childType) {
        return mapDescriptors.values().stream().filter(m -> m.getName().equalsIgnoreCase(childType)).findAny();
    }

    public Optional<EntityDescriptor> getEntityDescriptorByPlural(String plural) {
        return Optional.ofNullable(mapDescriptors.get(plural.toLowerCase()));
    }

    public String getPlural() {
        return rootDesc.getPlural().toLowerCase();
    }

    public String getVersion() {
        return version;
    }

    public String getRootType() {
        return rootDesc.getName();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDomainOwner() {
        return domainOwner;
    }

    public String getDomainContext() {
        return domainContext;
    }

    public Optional<ActionDescriptor> getRootAction(String action) {
        return Optional.ofNullable(rootActions.get(action));
    }

    public Optional<ActionDescriptor> getChildAction(String child, String action) {
        return Optional.ofNullable(childActions.get(child)).map(m -> m.get(action));
    }

    public static final class ActionDescriptor {

        public enum ActionType {
            COMMAND_HANDLER, EVENT_HANDLER, QUERY_HANDLER
        }
        private final String name;
        private final ActionType actionType;
        private final boolean fileUploadEnabled;
        private final Set<String> mimeTypes;

        public ActionDescriptor(String name, ActionType actionType) {
            this.name = name;
            this.actionType = actionType;
            this.fileUploadEnabled = false;
            this.mimeTypes = Collections.emptySet();
        }

        public ActionDescriptor(String name, ActionType actionType, boolean fileUploadEnabled, Set<String> mimeTypes) {
            this.name = name;
            this.actionType = actionType;
            this.fileUploadEnabled = fileUploadEnabled;
            this.mimeTypes = mimeTypes;
        }

        public ActionType getActionType() {
            return actionType;
        }

        public String getName() {
            return name;
        }

        public boolean isFileUploadEnabled() {
            return fileUploadEnabled;
        }

        public Set<String> getMimeTypes() {
            return mimeTypes;
        }
    }

    public static final class EntityDescriptor {

        private final String name;
        private final String plural;

        public EntityDescriptor(String name, String plural) {
            this.name = name;
            this.plural = plural;
        }

        public String getName() {
            return name;
        }

        public String getPlural() {
            return plural;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 89 * hash + Objects.hashCode(this.plural);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntityDescriptor other = (EntityDescriptor) obj;
            if (!Objects.equals(this.plural, other.plural)) {
                return false;
            }
            return true;
        }

    }
}
