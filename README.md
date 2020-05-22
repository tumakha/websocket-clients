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

