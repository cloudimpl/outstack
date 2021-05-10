/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.handler.DefaultDeleteCommandHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultRenameCommandHandler;
import com.cloudimpl.outstack.runtime.util.Util;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class ServiceProvider<T extends RootEntity, R> implements Function<Object, Publisher<?>> {

    private final Map<String, EntityCommandHandler> mapCmdHandlers = new HashMap<>();
    private final EventHandlerManager evtHandlerManager;
    private final Class<? extends RootEntity> rootType;
    private final EventRepositoy<T> eventRepository;
    private final EntityContextProvider<T> contextProvider;

    public ServiceProvider(Class<T> rootType, EventRepositoy<T> eventRepository,Function<Class<? extends RootEntity>,QueryOperations<?>> queryOperationSelector) {
        this.rootType = rootType;
        this.evtHandlerManager = new EventHandlerManager(rootType);
        this.eventRepository = eventRepository;
        contextProvider = new EntityContextProvider<>(this.eventRepository::loadEntityWithClone, eventRepository::generateTid, eventRepository,queryOperationSelector);
    }

    public void registerCommandHandler(Class<? extends EntityCommandHandler> handlerType) {

        validateHandler(handlerType.getSimpleName().toLowerCase(), rootType, Util.extractGenericParameter(handlerType, EntityCommandHandler.class, 0));
        EntityCommandHandler exist = mapCmdHandlers.putIfAbsent(handlerType.getSimpleName().toLowerCase(), Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>()));
        if (exist != null) {
            throw new ServiceProviderException("commad handler {0} already exist ", handlerType.getSimpleName());
        }
    }

    public void registerDefaultCmdHandlersForEntity(Class<? extends Entity> entityType) {
        validateHandler("defaultCommandHandlers", rootType, entityType);
        mapCmdHandlers.computeIfAbsent(("Delete" + entityType.getSimpleName()).toLowerCase(), s -> Util.createObject(DefaultDeleteCommandHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType)));
        mapCmdHandlers.computeIfAbsent(("Rename" + entityType.getSimpleName()).toLowerCase(), s -> Util.createObject(DefaultRenameCommandHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType)));
    }

    public void registerEventHandler(Class<? extends EntityEventHandler> handlerType) {
        this.evtHandlerManager.register(handlerType);
    }

    public Optional<EntityCommandHandler> getCmdHandler(String name) {
        return Optional.ofNullable(mapCmdHandlers.get(name.toLowerCase()));
    }
    
    public static void validateHandler(String name, Class<? extends RootEntity> rootType, Class<? extends Entity> type) {
        if (RootEntity.isMyType(type)) {
            if (type != rootType) {
                throw new ServiceProviderException("handler {0} root entity type {1} not matched with service provider type {2}", name, type.getName(), rootType.getName());
            }
        } else {
            Class<? extends RootEntity> root = Util.extractGenericParameter(type, ChildEntity.class, 0);
            if (root != rootType) {
                throw new ServiceProviderException("handler {0} root entity type {1} not matched with service provider type {2}", name, root.getName(), rootType.getName());
            }
        }

    }

    @Override
    public Publisher apply(Object input) {

        if (ICommand.class.isInstance(input)) {
            return applyCommand((ICommand) input);
        }else {
            return Mono.error(() -> new CommandException("invalid input received. {0}", input));
        }

    }

    private Publisher applyCommand(ICommand cmd) {
        try
        {
            return Mono.just(getCmdHandler(cmd.commandName()).orElseThrow(() -> new CommandException("command {0} not found", cmd.commandName().toLowerCase())).emit(contextProvider, cmd))
                .doOnNext(ct -> this.evtHandlerManager.emit((EntityContextProvider.Transaction)ct.getTx(), ct.getEvents()))
                .doOnNext(ct -> eventRepository.saveTx((EntityContextProvider.Transaction)ct.getTx()))
                .map(ct -> ct.getTx().getReply());
        }catch(Throwable thr)
        {
            thr.printStackTrace();
            return Mono.error(thr);
        }
        
    }

    public void applyEvent(Event event) {
        EntityContextProvider.Transaction<T> tx = contextProvider.createWritableTransaction(event.rootId(), event.tenantId());
        this.evtHandlerManager.emit(tx, Collections.singletonList(event));
        eventRepository.saveTx(tx);
    }

    public void validate(Predicate<String> pred, String name, String error) {
        if (pred.test(name)) {
            throw new ServiceProviderException(error);
        }
    }
}
