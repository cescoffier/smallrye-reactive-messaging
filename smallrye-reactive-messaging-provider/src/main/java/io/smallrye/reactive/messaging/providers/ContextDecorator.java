package io.smallrye.reactive.messaging.providers;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.impl.MessageLocal;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class ContextDecorator implements PublisherDecorator {
    @Override
    public Multi<? extends Message<?>> decorate(Multi<? extends Message<?>> publisher, String channelName) {
        return publisher
                .onItem().transformToUniAndConcatenate(message -> {
                    Optional<MessageLocal> local = message.getMetadata(MessageLocal.class);
                    return Uni.createFrom().emitter(emitter -> {
                        if (local.isPresent()) {
                            local.get().context().runOnContext(x -> emitter.complete(message));
                        } else {
                            emitter.complete(message);
                        }
                    });
                });
    }
}
