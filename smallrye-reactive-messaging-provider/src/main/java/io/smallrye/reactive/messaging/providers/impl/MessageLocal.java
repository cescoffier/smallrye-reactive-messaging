package io.smallrye.reactive.messaging.providers.impl;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.impl.ContextInternal;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MessageLocal {

    private final ContextInternal context;

    public MessageLocal(ContextInternal context) {
        this.context = context;
    }

    public ContextInternal context() {
        return context;
    }

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

    public static <T> Uni<T> invokeOnMessageContext(Message<?> incoming, BiConsumer<Message<?>, UniEmitter<? super T>> function) {
        Optional<MessageLocal> metadata = incoming != null  ? incoming.getMetadata(MessageLocal.class) : Optional.empty();
        if (metadata.isPresent()) {
            // Call function on Message's context
            return Uni.createFrom().emitter(emitter -> {
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
