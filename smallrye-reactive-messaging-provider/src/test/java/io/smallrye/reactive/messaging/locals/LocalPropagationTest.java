package io.smallrye.reactive.messaging.locals;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.WeldTestBaseWithoutTails;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.providers.ContextDecorator;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.smallrye.reactive.messaging.providers.impl.MessageLocal;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.impl.ContextInternal;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class LocalPropagationTest extends WeldTestBaseWithoutTails {

    @Test
    public void testLinearPipeline() {
        installConfig("src/test/resources/config/locals.properties");
        addBeanClass(ContextDecorator.class);
        addBeanClass(ConnectorEmittingOnContext.class);
        addBeanClass(ConnectorEmittingDirectly.class);
        addBeanClass(LinearPipeline.class);
        initialize();

        LinearPipeline bean = get(LinearPipeline.class);
        await().atMost(2, TimeUnit.MINUTES).until(() -> bean.getResults().size() >= 5);
        assertThat(bean.getResults()).containsExactly(2, 3, 4, 5, 6);

    }

    @Test
    public void testPipelineWithABlockingStage() {
        installConfig("src/test/resources/config/locals.properties");
        addBeanClass(ContextDecorator.class);
        addBeanClass(ConnectorEmittingOnContext.class);
        addBeanClass(ConnectorEmittingDirectly.class);
        addBeanClass(PipelineWithABlockingStage.class);
        initialize();

        PipelineWithABlockingStage bean = get(PipelineWithABlockingStage.class);
        await().atMost(2, TimeUnit.MINUTES).until(() -> bean.getResults().size() >= 5);
        assertThat(bean.getResults()).containsExactly(2, 3, 4, 5, 6);

    }

    // Test blocking not ordered

    // Test multiple blocking

    // Test broadcast

    // Test merge

    // Test broadcast + merge

    // Test with ack / nack going to another thread

    // Test when emission is not done on I/O thread

    @Connector("connector-emitting-on-context")
    public static class ConnectorEmittingOnContext implements InboundConnector {

        @Inject
        ExecutionHolder executionHolder;

        @Override
        public Publisher<? extends Message<?>> getPublisher(Config config) {
            Context context = executionHolder.vertx().getOrCreateContext();
            return Multi.createFrom().items(1, 2)
                    .map(Message::of)
                    .onItem().transformToUniAndConcatenate(i ->
                            Uni.createFrom().emitter(e -> context.runOnContext(() -> e.complete(i)))
                    );
        }
    }

    @Connector("connector-without-context")
    public static class ConnectorEmittingDirectly implements InboundConnector {

        @Override
        public Publisher<? extends Message<?>> getPublisher(Config config) {
            return Multi.createFrom().items(1, 2, 3, 4, 5)
                    .map(Message::of)
                    .onItem().transformToUniAndConcatenate(i ->
                            Uni.createFrom().emitter(e -> e.complete(i))
                    );
        }
    }


    @ApplicationScoped
    public static class LinearPipeline {

        private final List<Integer> list = new ArrayList<>();
        private final Set<String> uuids = new ConcurrentHashSet<>();


        @Incoming("data")
        @Outgoing("process")
        @Acknowledgment(Acknowledgment.Strategy.MANUAL)
        public Message<Integer> process(Message<Integer> input) {
            String value = UUID.randomUUID().toString();
            Vertx.currentContext().putLocal("uuid", value);
            Vertx.currentContext().putLocal("input", input.getPayload());

            System.out.println("received " + input.getPayload() + " --> " + value + " on " + Vertx.currentContext());
            return input.withPayload(input.getPayload() + 1);
        }


        @Incoming("process")
        @Outgoing("after-process")
        public Integer handle(int payload) {
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            assertThat(uuids.add(uuid)).isTrue();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(payload);
            return payload;
        }

        @Incoming("after-process")
        @Outgoing("sink")
        public Integer afterProcess(int payload) {
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(payload);
            return payload;
        }


        @Incoming("sink")
        public void sink(int val) {
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(val);
            list.add(val);
        }

        public List<Integer> getResults() {
            return list;
        }
    }

    @ApplicationScoped
    public static class PipelineWithABlockingStage {

        private final List<Integer> list = new ArrayList<>();
        private final Set<String> uuids = new ConcurrentHashSet<>();


        @Incoming("data")
        @Outgoing("process")
        @Acknowledgment(Acknowledgment.Strategy.MANUAL)
        public Message<Integer> process(Message<Integer> input) {
            String value = UUID.randomUUID().toString();
            assertThat((String) Vertx.currentContext().getLocal("uuid")).isNull();
            Vertx.currentContext().putLocal("uuid", value);
            Vertx.currentContext().putLocal("input", input.getPayload());

            assertThat(input.getMetadata(MessageLocal.class)).isPresent();

            System.out.println("process: " + input.getPayload() + " --> " + value + " on " + Vertx.currentContext());
            return input.withPayload(input.getPayload() + 1);
        }

        @Incoming("process")
        @Outgoing("after-process")
        @Blocking
        public Integer handle(int payload) {
            System.out.println("handle: " + payload + " - Internal context: " + ((ContextInternal) Vertx.currentContext().getDelegate()).localContextData());
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            assertThat(uuids.add(uuid)).isTrue();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(payload);
            return payload;
        }

        @Incoming("after-process")
        @Outgoing("sink")
        public Integer afterProcess(int payload) {
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(payload);
            return payload;
        }


        @Incoming("sink")
        public void sink(int val) {
            String uuid = Vertx.currentContext().getLocal("uuid");
            assertThat(uuid).isNotNull();

            int p = Vertx.currentContext().getLocal("input");
            assertThat(p + 1).isEqualTo(val);
            list.add(val);
        }

        public List<Integer> getResults() {
            return list;
        }
    }

}
