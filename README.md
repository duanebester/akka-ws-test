# Akka Streams + WebSockets

### Running

```
sbt ~reStart
```

## Overview

When running the server, there will be a simple file upload input on localhost:8080. Uploading an image goes through several processing stages. We update the user on the stage of processing via a WebSocket. The WebSocket flow is built from a `Sink.ignore` and an `actorRef` that we `preMaterialize()`
