package inbound;

import io.smallrye.reactive.messaging.kafka.Record;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Converters {


    // tag::code[]
    @Incoming("topic-a")
    public void consume(Record<String, String> record) {
        String key = record.key(); // Can be `null` if the incoming record has no key
        String value = record.value(); // Can be `null` if the incoming record has no value
    }

    @Incoming("topic-b")
    public void consume(ConsumerRecord<String, String> record) {
        String key = record.key(); // Can be `null` if the incoming record has no key
        String value = record.value(); // Can be `null` if the incoming record has no value
        String topic = record.topic();
        int partition = record.partition();
        // ...
    }
    // end::code[]

}
