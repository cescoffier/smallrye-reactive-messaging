package io.smallrye.reactive.messaging.providers.impl;

import io.vertx.core.impl.ContextInternal;

public class MessageLocal {

    private final ContextInternal context;

    public MessageLocal(ContextInternal context) {
        this.context = context;
    }

    public ContextInternal context() {
        return context;
    }


}
