package io.smallrye.reactive.messaging.providers.locals;

import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;

import io.smallrye.common.annotation.CheckReturnValue;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;

public interface ContextAwareMessage<T> extends Message<T> {

    static <T> Message<T> withContextMetadata(Message<T> message) {
        return message.withMetadata(addContextMetadata(message.getMetadata()));
    }

    static Metadata addContextMetadata(Metadata metadata) {
        ContextInternal ci = (ContextInternal) Vertx.currentContext();
        if (ci == null) {
            // nothing to do not on vert.x context
            return metadata;
        } else {
            // TODO check if metadata contains already a local context
            ContextInternal view = ci.duplicate();
            return metadata.with(new LocalContextMetadata(view));
        }
    }

    @CheckReturnValue
    default Metadata captureContextMetadata() {
        return addContextMetadata(getMetadata());
    }

    @CheckReturnValue
    default Metadata captureContextMetadata(Metadata metadata) {
        return addContextMetadata(metadata);
    }

    default Optional<LocalContextMetadata> getContextMetadata() {
        return getMetadata().get(LocalContextMetadata.class);
    }
}
