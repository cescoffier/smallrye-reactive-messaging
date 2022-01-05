package io.smallrye.reactive.messaging.rabbitmq;

import org.jboss.weld.environment.se.Weld;

import io.smallrye.config.inject.ConfigExtension;
import io.smallrye.reactive.messaging.providers.MediatorFactory;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.smallrye.reactive.messaging.providers.connectors.WorkerPoolRegistry;
import io.smallrye.reactive.messaging.providers.extension.EmitterImpl;
import io.smallrye.reactive.messaging.providers.extension.MediatorManager;
import io.smallrye.reactive.messaging.providers.extension.ReactiveMessagingExtension;
import io.smallrye.reactive.messaging.providers.impl.ConfiguredChannelFactory;
import io.smallrye.reactive.messaging.providers.impl.ConnectorFactories;
import io.smallrye.reactive.messaging.providers.impl.InternalChannelRegistry;
import io.smallrye.reactive.messaging.providers.wiring.Wiring;

public class BaseWeld {

    private BaseWeld() {
        // Avoid direct instantiation.
    }

    public static Weld getWeld() {
        Weld weld = new Weld();
        weld.disableDiscovery();
        weld.addBeanClass(MediatorFactory.class);
        weld.addBeanClass(MediatorManager.class);
        weld.addBeanClass(InternalChannelRegistry.class);
        weld.addBeanClass(ConnectorFactories.class);
        weld.addBeanClass(ConfiguredChannelFactory.class);
        weld.addBeanClass(WorkerPoolRegistry.class);
        weld.addBeanClass(ExecutionHolder.class);
        weld.addBeanClass(Wiring.class);
        weld.addPackages(EmitterImpl.class.getPackage());
        weld.addExtension(new ReactiveMessagingExtension());
        weld.addExtension(new ConfigExtension());
        weld.addBeanClass(RabbitMQConnector.class);
        return weld;
    }
}
