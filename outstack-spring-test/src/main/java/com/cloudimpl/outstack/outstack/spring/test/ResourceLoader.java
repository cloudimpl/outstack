/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.outstack.spring.test;

import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.EntityEventHandler;
import com.cloudimpl.outstack.runtime.domainspec.Command;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nuwan
 */
public class ResourceLoader {

    private ScanResult scanResult;
    private final Map<String, Class<? extends Entity>> mapEntities = new HashMap<>();
    private final Map<String, Class<? extends Event>> mapEvents = new HashMap<>();
    private final Map<String, Class<? extends Command>> mapCommands = new HashMap<>();
    private final Map<String, Class<? extends EntityCommandHandler<? extends Entity, ? extends Command, ?>>> mapCmdHandlers = new HashMap<>();
    private final Map<String, Class<? extends EntityEventHandler>> mapEvtHandlers = new HashMap<>();
    
    public ResourceLoader() {
        loadEntities();
        loadEvents();
        loadCommands();
        loadCmdHandlers();
        loadEvtHandlers();
    }

    private ScanResult getScanResult() {
        if (scanResult == null) {
            scanResult = new ClassGraph().enableClassInfo().scan();
        }
        return scanResult;
    }

    private void loadEntities() {
        ClassInfoList controlClasses = getScanResult().getSubclasses(Entity.class.getName());
        for (Class<?> type : controlClasses.loadClasses()) {
            mapEntities.put(type.getSimpleName(), (Class<? extends Entity>) type);
        }
    }

    private void loadEvents() {
        ClassInfoList controlClasses = getScanResult().getSubclasses(Event.class.getName());
        for (Class<?> type : controlClasses.loadClasses()) {
            mapEvents.put(type.getSimpleName(), (Class<? extends Event>) type);
        }
    }

    private void loadCommands() {
        ClassInfoList controlClasses = getScanResult().getSubclasses(Command.class.getName());
        for (Class<?> type : controlClasses.loadClasses()) {
            mapCommands.put(type.getSimpleName(), (Class<? extends Command>) type);
        }
    }
    
    private void loadCmdHandlers() {
        ClassInfoList controlClasses = getScanResult().getSubclasses(EntityCommandHandler.class.getName());
        for (Class<?> type : controlClasses.loadClasses()) {
            mapCmdHandlers.put(type.getSimpleName(), (Class<? extends EntityCommandHandler<? extends Entity, ? extends Command, ?>>) type);
        }
    }
    
    private void loadEvtHandlers() {
        ClassInfoList controlClasses = getScanResult().getSubclasses(EntityEventHandler.class.getName());
        for (Class<?> type : controlClasses.loadClasses()) {
            mapEvtHandlers.put(type.getSimpleName(), (Class<? extends EntityEventHandler>) type);
        }
    }

    public Map<String, Class<? extends Entity>> getMapEntities() {
        return mapEntities;
    }

    public Map<String, Class<? extends Event>> getMapEvents() {
        return mapEvents;
    }

    public Map<String, Class<? extends Command>> getMapCommands() {
        return mapCommands;
    }

    public Map<String, Class<? extends EntityCommandHandler<? extends Entity, ? extends Command, ?>>> getMapCmdHandlers() {
        return mapCmdHandlers;
    }

    public Map<String, Class<? extends EntityEventHandler>> getMapEvtHandlers() {
        return mapEvtHandlers;
    }
}
