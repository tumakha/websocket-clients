package websocket.clients.impl.akka;

import akka.Done;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;
import akka.http.javadsl.settings.ClientConnectionSettings;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import lombok.extern.slf4j.Slf4j;
import websocket.clients.WebSocketClient;
import websocket.clients.impl.java11.InsecureSslContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author Yuriy Tumakha
 */
@Slf4j
public class AkkaWebSocketClient implements WebSocketClient {

    private final ActorSystem system = ActorSystem.create();
    private final Http http = Http.get(system);
    private final ConnectionContext connectionContext = ConnectionContext.https(InsecureSslContext.SSL_CONTEXT);
    private final ClientConnectionSettings connectionSettings = ClientConnectionSettings.create(system);
    private CompletableFuture<Optional<Message>> source;

    private String endpoint;
    private Consumer<String> messageReader;

    @Override
    public String getName() {
        return "Akka HTTP";
    }

    @Override
    public void connect(String endpoint, Consumer<String> messageReader) {
        this.endpoint = endpoint;
        this.messageReader = messageReader;
    }

    @Override
    public void sendMessage(String text) {
        final Source<Message, CompletableFuture<Optional<Message>>> writerSource =
                Source.from(List.<Message>of(TextMessage.create(text)))
                        .concatMat(Source.maybe(), Keep.right());

        Sink<Message, CompletionStage<Done>> readerSink =
                Sink.foreach(message -> messageReader.accept(message.asTextMessage().getStrictText()));

        Flow<Message, Message, CompletionStage<WebSocketUpgradeResponse>> webSocketFlow =
                http.webSocketClientFlow(
                        WebSocketRequest.create(endpoint),
                        connectionContext,
                        Optional.empty(),
                        connectionSettings,
                        system.log()
                );

        Pair<CompletableFuture<Optional<Message>>, CompletionStage<WebSocketUpgradeResponse>> pair =
                writerSource.viaMat(webSocketFlow, Keep.both())
                        .toMat(readerSink, Keep.left())
                        .run(system);

        source = pair.first();

        // The first value in the pair is a CompletionStage<WebSocketUpgradeResponse> that
        // completes when the WebSocket request has connected successfully (or failed)
        CompletionStage<WebSocketUpgradeResponse> upgradeCompletion = pair.second();

        CompletionStage<Done> connected = upgradeCompletion.thenApply(upgrade ->
        {
            // like a regular http request we access response status which is available via upgrade.response.status
            // status code 101 (Switching Protocols) indicates that server support WebSockets
            if (upgrade.response().status().equals(StatusCodes.SWITCHING_PROTOCOLS)) {
                return Done.getInstance();
            } else {
                throw new RuntimeException(("Connection failed: " + upgrade.response().status()));
            }
        });

        connected.thenAccept(done -> System.out.println("Connected"));
    }

    @Override
    public void close() {
        source.complete(Optional.empty());
        system.terminate();
    }

}
