## websocket-clients

WebSocket clients performance test

#### Prerequisites

Java 13+, Gradle 6 or gradle-wrapper

#### Build

    gradle build

#### Run WebSocket server

    java -jar ./websocket-server/build/libs/websocket-server.jar
    
Report will be saved as `websocket-time-on-server.csv`

#### Run clients test

    java -jar ./clients-test/build/libs/websocket-clients.jar
    
Report will be saved as `websocket-clients-performance.csv`

#### Test results

