Overview
========
A simple DSL to configure and define a Web Server

There are two variations of the server:
* __Mock Server__: A Programmable REST server, allows to define custom behavior on each REST API, responds with *404 Not Found* on undefinded APIs.
* __Stub Server__: Responds *200 OK* on all REST APIs and records all incoming requests.


## Create Web Server

Import Factory
```scala
    import com.wix.e2e.http.server.WebServerFactory._
```

### Mock Server

The mock server is useful for cases in which the server is a part of an end-to-end transaction in which it is expected to get some inputs and reply with a specific output that can later on be validated from the outside.

```scala
    // start a server on a dynamic open port
    val handler: RequestHandler = { case r: HttpRequest => HttpResponse()  }
    val server = aMockWebServerWith(handler).build
                                            .start()
                                            
    // start on a custom port                                            
    val somePort = 11111
    val serverOnCustomPort = aMockWebServerWith(handler).onPort(somePort)
                                                        .build
                                                        .start()
                                            
```

To program our mock server we will need to define handlers. A Handler is a function that receives a request and returns some response.

For example, a server that listens to requests on `/somePath` and responds with `OK!!!`.
A server can handle one or more handlers and it will use the first handler that is defined for the incoming request.

```scala
    val okHandler = { case r: HttpRequest if r.uri.path.toString.endsWith("somePath") => HttpResponse(entity = "OK!!!") }
```

### Stub Server

The stub server will record all incoming requests and respond with a 200OK to all requests.
You will probably need this simple implementation in case you have an external server being called from your service while the output from this service is not being used in the transaction or simply not accessible from the outside.
For example: you are triggering a REST API that sends a mail.

Create the server
```scala
    // start a server on a dynamic open port
    val server = aStubWebServer.build
                               .start()
    
    // use custom port
    val somePort = 11111
    val serverOnCustomPort = aStubWebServer.onPort(somePort)
                                           .build
                                           .start()
```

A Stub server can, but is not required to, have custom handlers (the same as the mock server)

```scala
    val someHandler = // create your own
    val anotherHandler = // create your own

    val server = aStubWebServer.addHandler(someHandler)                  // add one
                               .addHandlers(someHandler, anotherHandler) // add more than one handler
                               .build
                               .start()


```
#### Editing handlers in test
You can update the handlers by calling : 
```scala
val newHandler = // create your own
mockWebServer.replaceWith(newHandler)
```
This will reset the handlers and set it to the new one 
Or you can add handlers to the existing ones : 
```scala
val newHandler = // create your own
mockWebServer.appendAll(newHandler)
```

#### Recorded Requests

To view the recorded requests just access the `recordedRequests` member:
```scala

    val server = // start server
    
    val requests = server.recordedRequests
    
    // you can also reset the recorded requests between tests
    server.clearRecordedRequests()

```

To validate incoming requests use the included [Specs<sup>2</sup> Matcher Suite](./WEBSERVER_MATCHERS.md).
