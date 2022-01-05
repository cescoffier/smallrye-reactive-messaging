package inbound;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class IncomingCamelMetadataExample {

//    @Incoming("files")
//    public CompletionStage<Void> consume(Message<GenericFile<File>> msg) {
//        Optional<IncomingExchangeMetadata> metadata = msg.getMetadata(IncomingExchangeMetadata.class);
//        if (metadata.isPresent()) {
//            // Retrieve the camel exchange:
//            Exchange exchange = metadata.get().getExchange();
//        }
//        return msg.ack();
//    }


}
