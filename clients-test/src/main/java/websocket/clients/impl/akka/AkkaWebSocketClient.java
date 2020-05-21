package websocket.clients.impl.akka;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.TextMessage;
import akka.http.javadsl.model.ws.WebSocketRequest;
import akka.http.javadsl.model.ws.WebSocketUpgradeResponse;
import akka.http.javadsl.settings.ClientConnectionSettings;
import akka.stream.javadsl.*;
import websocket.clients.WebSocketClient;
import websocket.clients.impl.java11.InsecureSslContext;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author Yuriy Tumakha
 */
public class AkkaWebSocketClient implements WebSocketClient {

    private final ActorSystem system = ActorSystem.create();
    private final Http http = Http.get(system);
    private final ConnectionContext connectionContext = ConnectionContext.https(InsecureSslContext.SSL_CONTEXT);
    private final ClientConnectionSettings connectionSettings = ClientConnectionSettings.create(system);
    private final SubmissionPublisher<String> msgPublisher = new SubmissionPublisher<>(newSingleThreadExecutor(), 2);

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

        final Source<Message, NotUsed> writerSource = JavaFlowSupport.Source.fromPublisher(msgPublisher)
                .map(TextMessage::create);

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

        CompletionStage<WebSocketUpgradeResponse> upgradeCompletion =
                writerSource.viaMat(webSocketFlow, Keep.right())
                        .toMat(readerSink, Keep.left())
                        .run(system);

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
    public void sendMessage(String text) {
        msgPublisher.submit(text);
    }

    @Override
    public void close() {
        msgPublisher.close();
        system.terminate();
    }

}
