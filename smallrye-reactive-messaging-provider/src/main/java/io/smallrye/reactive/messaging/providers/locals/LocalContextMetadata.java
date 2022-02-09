package io.smallrye.reactive.messaging.providers.locals;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

public class LocalContextMetadata {

    private final ContextInternal context;

    public LocalContextMetadata(ContextInternal context) {
        this.context = context;
    }

    public ContextInternal context() {
        return context;
    }

    public static Map<Object, Object> getContextualData(Message<?> message) {
        return message.getMetadata().get(LocalContextMetadata.class)
                .map(c -> (Map<Object, Object>) c.context().localContextData())
                .orElse(Collections.emptyMap());
    }

    // TODO Flatten identity transformation (onItem().transformToUni(u -> u)))
    // TODO Use operators instead of emitter

    // TODO Replace with an operator
    public static <T> Uni<T> invokeOnMessageContext(Message<?> incoming, Function<Message<?>, T> function) {
        return invokeOnMessageContext(incoming, (message, emitter) -> {
            T res;
            try {
                res = function.apply(message);
            } catch (Exception failure) {
                emitter.fail(failure);
                return;
            }
            emitter.complete(res);
        });
    }

    public static <T> Uni<T> invokeOnMessageContext(Message<?> incoming,
            BiConsumer<Message<?>, UniEmitter<? super T>> function) {
        Optional<LocalContextMetadata> metadata = incoming != null ? incoming.getMetadata().get(LocalContextMetadata.class)
                : Optional.empty();
        if (metadata.isPresent()) {
            // Call function on Message's context
            // TODO Replace with an operator
            return Uni.createFrom().emitter(emitter -> {
                Context current = Vertx.currentContext();
                if (current != null && current == metadata.get().context) {
                    // Direct call, we are already on the right context.
                    try {
                        function.accept(incoming, emitter);
                    } catch (Exception e) {
                        emitter.fail(e);
                    }
                    return;
                }
                // Run function on the message context
                metadata.get().context.runOnContext(x -> {
                    try {
                        function.accept(incoming, emitter);
                    } catch (Exception e) {
                        emitter.fail(e);
                    }
                });

            });
        } else {
            return Uni.createFrom().emitter(emitter -> function.accept(incoming, emitter));
        }
    }
}
