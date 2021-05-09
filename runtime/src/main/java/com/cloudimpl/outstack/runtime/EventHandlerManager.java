/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import static com.cloudimpl.outstack.runtime.ServiceProvider.validateHandler;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class EventHandlerManager<T extends RootEntity> {

    private final Class<T> rootType;
    private final Map<String, List<EntityEventHandler>> mapEvtGroupHandlers;
    private final Map<String, EntityEventHandler> mapEvtHandlers;

    public EventHandlerManager(Class<T> rootType) {
        this.rootType = rootType;
        this.mapEvtHandlers = new HashMap<>();
        this.mapEvtGroupHandlers = new HashMap<>();
    }

    public void register(Class<? extends EntityEventHandler> handlerType) {
        validateHandler(handlerType.getSimpleName().toLowerCase(), rootType, Util.extractGenericParameter(handlerType, EntityEventHandler.class, 0));
        EntityEventHandler exist = mapEvtHandlers.putIfAbsent(handlerType.getSimpleName().toLowerCase(), Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>()));
        if (exist != null) {
            throw new ServiceProviderException("event handler {0} already exist ", handlerType.getSimpleName());
        }
        Class<? extends Event> eventType = Util.extractGenericParameter(handlerType, EntityEventHandler.class, 1);
        List<EntityEventHandler> handlers = mapEvtGroupHandlers.get(eventType.getSimpleName());
        if (handlers == null) {
            handlers = new LinkedList<>();
            mapEvtGroupHandlers.put(eventType.getSimpleName(), handlers);
        }
        handlers.add(Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>()));

    }

    public void emit(EntityContextProvider.Transaction <T> transaction,List<Event> events) {
        List<EntityContext> contexts = new LinkedList<>();
         for(Event ev: events)
         {
             emitHandler(transaction, contexts, ev);
         }
         for(EntityContext context: contexts)
         {
             emit(transaction,context.getEvents());
         }
    }
    
    private void emitHandler(EntityContextProvider.Transaction <T> tx,List<EntityContext> contexts,Event event)
    {
        List<EntityEventHandler> handlers = mapEvtGroupHandlers.get(event.getClass().getSimpleName());
        if(handlers != null)
        {
            handlers.forEach(h->contexts.add(h.emit(tx, event)));
        }
    }
}
