package processor;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CamelProcessor {

    @Incoming("mynatssubject")
    @Outgoing("mykafkatopic")
    public byte[] process(byte[] message) {
        // do some logic
        return message;
    }

}
