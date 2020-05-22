## websocket-clients

[![Build](https://github.com/tumakha/websocket-clients/workflows/websocket-clients/badge.svg)](https://github.com/tumakha/websocket-clients/actions)

WebSocket clients performance test

#### Prerequisites

Java 13+, Gradle 6 or gradle-wrapper

#### Build

    gradle build

#### Run WebSocket server

run Java-WebSocket server

    java -jar ./websocket-server/build/libs/websocket-server.jar

or run Netty server

    java -jar ./websocket-server/build/libs/websocket-server.jar netty

#### Run clients test

    java -jar ./clients-test/build/libs/websocket-clients.jar
    
Report will be saved as `websocket-clients-performance.csv`

#### Test results

90% of request processed under following times in us (microseconds) 

- 125.5 - com.neovisionaries.ws.client.WebSocket
- 128.8 - Java-WebSocket
- 129.1 - Jetty
- 137.8 - Spring StandardWebSocketClient
- 137.9 - AsyncHttpClient
- 143.4 - Spring ReactorNettyWebSocketClient
- 144.7 - Java 11 java.net.http.WebSocket
- 145.7 - Netty
- 454398.278 - Akka HTTP
