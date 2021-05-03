/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class ContextTransaction {
    private final List<Event> eventList;
    private final List<Entity> entityList;

    public ContextTransaction() {
        this.eventList = new LinkedList<>();
        this.entityList = new LinkedList<>();
    }
    
    public void addEntity(Entity entity)
    {
        this.entityList.add(entity);
    }
    
    public void addEvent(Event event)
    {
        this.eventList.add(event);
    }
}
