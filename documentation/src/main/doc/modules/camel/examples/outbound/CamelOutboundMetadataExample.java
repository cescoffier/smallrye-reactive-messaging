package outbound;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class CamelOutboundMetadataExample {

//    private Random random = new Random();
//
//    @Outgoing("prices")
//    public Multi<Message<String>> generate() {
//        // tag::code[]
//        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
//            .map(x -> random.nextDouble())
//            .map(p -> Double.toString(p))
//            .map(s ->
//                Message.of(s)
//                    .addMetadata(new OutgoingExchangeMetadata().putProperty("my-property", "my-value"))
//            );
//        // end::code[]
//    }


}
