/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.runtime;

import com.cloudimpl.outstack.runtime.common.GsonCodecRuntime;
import com.cloudimpl.outstack.runtime.domainspec.ChildEntity;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import com.cloudimpl.outstack.runtime.domainspec.Event;
import com.cloudimpl.outstack.runtime.domainspec.ICommand;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import com.cloudimpl.outstack.runtime.handler.DefaultDeleteCommandHandler;
import com.cloudimpl.outstack.runtime.handler.DefaultRenameCommandHandler;
import com.cloudimpl.outstack.runtime.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.internal.LinkedTreeMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author nuwan
 * @param <T>
 */
public class ServiceProvider<T extends RootEntity, R> implements Function<Object, Publisher<?>> {

    private final Map<String, EntityCommandHandler> mapCmdHandlers = new HashMap<>();
    private final EventHandlerManager evtHandlerManager;
    private final Class<T> rootType;
    private final EventRepositoy<T> eventRepository;
    private final EntityContextProvider<T> contextProvider;
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final Supplier<Consumer> injector;
    public ServiceProvider(Class<T> rootType, EventRepositoy<T> eventRepository, Function<Class<? extends RootEntity>, QueryOperations<?>> queryOperationSelector,Supplier<BiFunction<String, Object, Mono>> requestHandler,Supplier<Consumer> injector) {
        this.rootType = rootType;
        this.evtHandlerManager = new EventHandlerManager(rootType);
        this.eventRepository = eventRepository;
        this.injector = injector;
        contextProvider = new EntityContextProvider<>(rootType, this.eventRepository::loadEntityWithClone, eventRepository::generateTid, eventRepository, queryOperationSelector,requestHandler);
    }

    public void registerCommandHandler(Class<? extends EntityCommandHandler> handlerType) {

        validateHandler(handlerType.getSimpleName().toLowerCase(), rootType, Util.extractGenericParameter(handlerType, EntityCommandHandler.class, 0));
        EntityCommandHandler handler = Util.createObject(handlerType, new Util.VarArg<>(), new Util.VarArg<>());
        injector.get().accept(handler);
        EntityCommandHandler exist = mapCmdHandlers.putIfAbsent(handlerType.getSimpleName().toLowerCase(), handler);
        if (exist != null) {
            throw new ServiceProviderException("commad handler {0} already exist ", handlerType.getSimpleName());
        }
    }

    public void registerDefaultCmdHandlersForEntity(Class<? extends Entity> entityType) {
        validateHandler("defaultCommandHandlers", rootType, entityType);
        EntityCommandHandler handler = Util.createObject(DefaultDeleteCommandHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType));
        injector.get().accept(handler);
        mapCmdHandlers.computeIfAbsent(("Delete" + entityType.getSimpleName()).toLowerCase(), s -> handler);
        EntityCommandHandler handler2 = Util.createObject(DefaultRenameCommandHandler.class, new Util.VarArg<>(entityType.getClass()), new Util.VarArg<>(entityType));
        injector.get().accept(handler2);
        mapCmdHandlers.computeIfAbsent(("Rename" + entityType.getSimpleName()).toLowerCase(), s -> handler2);
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
        } else if (LinkedTreeMap.class.isInstance(input)) {
            return applyCommand(GsonCodecRuntime.decodeTree(CommandWrapper.class, (LinkedTreeMap) input));
        } else {
            return Mono.error(() -> new CommandException("invalid input received. {0}", input));
        }
    }

    private Publisher applyCommand(ICommand cmd) {
        try {
            EntityCommandHandler handler = getCmdHandler(cmd.commandName()).orElseThrow(() -> new CommandException("command {0} not found", cmd.commandName().toLowerCase()));
            if (AsyncEntityCommandHandler.class.isInstance(handler)) {
                Mono<EntityContext> mono = AsyncEntityCommandHandler.class.cast(handler).<EntityContext>emitAsync(contextProvider, cmd);
                return mono.doOnNext(ct -> this.evtHandlerManager.emit((EntityContextProvider.Transaction) ct.getTx(), ct.getEvents()))
                        .doOnNext(ct -> eventRepository.saveTx((ct.getTx())))
                        .flatMap(ct -> resolveReply(ct.getTx().getReply()))
                        .map(r -> encode(cmd, r)).doOnError(e -> ((Throwable)e).printStackTrace());
            } else if(UnboundedCommandHandler.class.isInstance(handler)) {
                return UnboundedCommandHandler.class.cast(handler).<EntityContext>emitAsync(contextProvider, cmd)
                        .flatMap(ct -> Flux.fromIterable(((EntityContext)ct).getTx().getTxList())
                                .doOnNext(tx -> this.evtHandlerManager.emit((EntityContextProvider.Transaction) tx, ((EntityContextProvider.Transaction<?>) tx).getEventList()))
                                .doOnNext(tx -> eventRepository.saveTx((ITransaction)tx)).collectList().map(list -> ct))
                        .flatMap(ct -> resolveReply(((EntityContext)ct).getTx().getReply()))
                        .map(r -> encode(cmd, r)).doOnError(e -> ((Throwable)e).printStackTrace());
            } else {
                return Mono.just(handler.emit(contextProvider, cmd))
                        .doOnNext(ct -> this.evtHandlerManager.emit((EntityContextProvider.Transaction) ct.getTx(), ct.getEvents()))
                        .doOnNext(ct -> eventRepository.saveTx((ct.getTx())))
                        .flatMap(ct -> resolveReply(ct.getTx().getReply()))
                        .map(r -> encode(cmd, r)).doOnError(e -> ((Throwable)e).printStackTrace());
            }

        } catch (Throwable thr) {
            thr.printStackTrace();
            return Mono.error(thr);
//            if (RuntimeException.class.isInstance(thr)) {
//                throw thr;
//            }
            //  throw thr;
            // throw new RuntimeException(thr);
        }
    }

    private Mono resolveReply(Object reply) {
        if (Mono.class.isInstance(reply)) {
            return (Mono) reply;
        } else {
            return Mono.just(reply);
        }
    }

    private Object encode(ICommand cmd, Object reply) {
        if (CommandWrapper.class.isInstance(cmd)) {
            //return objectMapper.writeValueAsString(reply);
            return objectMapper.convertValue(reply, LinkedHashMap.class);

        } else {
            return reply;
        }
    }

    public void applyEvent(Event event) {
        EntityContextProvider.Transaction<T> tx = contextProvider.createWritableTransaction(event.rootId(), event.tenantId(), false);
        this.evtHandlerManager.emit(tx, Collections.singletonList(event));
        eventRepository.saveTx(tx);
    }

    public void validate(Predicate<String> pred, String name, String error) {
        if (pred.test(name)) {
            throw new ServiceProviderException(error);
        }
    }
}
