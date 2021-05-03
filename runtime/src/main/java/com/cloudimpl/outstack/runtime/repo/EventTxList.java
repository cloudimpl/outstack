/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime.repo;

import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author nuwan
 */
public class EventTxList {
    private final List<Event> events;
    private final List<Entity> entities;
    public EventTxList() {
        events = new LinkedList<>();
        entities = new LinkedList<>();
    }
   
    public void addEvent(Event e)
    {
        this.events.add(e);
    }

    public void addEntity(Entity e)
    {
        this.entities.add(e);
    }
    
    public List<Event> getEvents() {
        return events;
    }

    public List<Entity> getEntities() {
        return entities;
    }
    
}
