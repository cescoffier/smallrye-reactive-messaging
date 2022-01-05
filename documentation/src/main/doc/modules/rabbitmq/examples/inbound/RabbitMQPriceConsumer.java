package inbound;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RabbitMQPriceConsumer {

    @Incoming("prices")
    public void consume(String price) {
        // process your price.
    }

}
