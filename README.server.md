Overview
========
A simple DSL to configure and define a Web Server

There are two variations of the server:
* __Mock Server__: A Programmable REST server, allows to define custom behavior on each REST api, responds *404 Not Found* on undefinded api's.
* __Stub Server__: Respond *200 OK* on all REST api's and records all incoming requests


##Create Web Server

Import Factory
```scala
    import com.wix.e2e.http.server.WebServerFactory._
```

### Mock Server

The mock server is useful for cases in which the server is a part of and e2e transaction, so it's expected to get some inputs and reply with a specific output which can later on be validated from the outside.

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

To program our mock server we will need to define handlers. Handler is a function that receives a request and respond with some response.
For example, if we want to have a request that listens to `/somePath` and respond with `OK!!!`
A server can handle one or more handlers and it will use the first handler that is defined for the incoming request. 

```scala
    val okHandler = { case r: HttpRequest if r.uri.path.toString.endsWith("somePath") => HttpResponse("OK!!!") }
```


### Stub Server

The stub server will record all incoming requests and respond with a 200OK to all requests. 
You will probably need this simple implementation in case you have an external server being called from your service while the output from this service is not being used in the transaction or simply not accessible from the outside.
For example: you are triggering a REST API that sends a mail.

Create server
```scala
    // start a server on a dynamic open port
    val server = aStubWebServer.build
                               .start()
    
    // use custom port
    val somePort = 11111
    val serverOnCustomPort = aStubWebServer.build
                                           .onPort(somePort)
                                           .start()
```

Stub server can, but not require, to have custom handlers (same like the mock server)

```scala
    val someHandler = // create your own
    val anotherHandler = // create your own
//  def addHandlers(handler: RequestHandler, handlers: RequestHandler*) = new StubWebServerBuilder(handlers = this.handlers ++ (handler +: handlers), port)
//  def addHandler(handler: RequestHandler) = addHandlers(handler)

    val server = aStubWebServer.build
                               .addHandler(someHandler)                  // add one
                               .addHandlers(someHandler, anotherHandler) // add more than one handler     
                               .start()


```


####Recorded Requests

To view the recorded requests just access the recordedRequests member:
```scala

    val server = // start server
    
    val requests = server.recordedRequests
    
    // you can also reset the recorded requests between tests
    server.clearRecordedRequests()

```

To validate incoming requests use the included []Specs<sup>2</sup> Matcher Suite](./README.matchers.md).