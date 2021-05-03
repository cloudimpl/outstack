/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.spring.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author nuwan
 */
public class SpringServiceDescriptor {
    private final String rootType;
    private final Map<String,ActionDescriptor> rootActions;
    private final Map<String,Map<String,ActionDescriptor>> childActions;
    private final String plural;
    private final String version;
    private final boolean isTenant;
    public SpringServiceDescriptor(String rootType,String version,String plural,boolean isTenant) {
        this.rootType = rootType;
        this.plural = plural;
        this.version = version;
        this.isTenant = isTenant;
        this.rootActions = new HashMap<>();
        this.childActions = new HashMap<>();
    }
    
    public void putRootAction(ActionDescriptor action)
    {
        this.rootActions.put(action.getName(),action);
    }
    
    public void putChildAction(String child,ActionDescriptor action)
    {
       Map<String,ActionDescriptor> map = childActions.get(child);
       if(map == null)
       {
           map = new HashMap<>();
           childActions.put(child, map);
       }
       map.put(action.getName(),action);
       
    }

    public boolean isTenantService()
    {
        return isTenant;
    }
    
    public String getPlural()
    {
        return plural;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String getRootType() {
        return rootType;
    }
    
    public Optional<ActionDescriptor> getRootAction(String action)
    {
        return Optional.ofNullable(rootActions.get(action));
    }
    
    public Optional<ActionDescriptor> getChildAction(String child,String action)
    {
        return Optional.ofNullable(childActions.get(child)).map(m->m.get(action));
    }
    
    public static final class ActionDescriptor
    {
        public enum ActionType { COMMAND_HANDLER,EVENT_HANDLER,QUERY_HANDLER }
        private final String name;
        private final ActionType actionType;

        public ActionDescriptor(String name, ActionType actionType) {
            this.name = name;
            this.actionType = actionType;
        }

        public ActionType getActionType() {
            return actionType;
        }

        public String getName() {
            return name;
        }
        
        
    }
}
