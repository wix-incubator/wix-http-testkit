# Wix HTTP Testkit

##Mock Web Server
Server created in order to simulate a simple web server with specific behavior.
The default behavior is to answer requests, if behavior is not defined the server will return 404 Not found by default.

Create server
```
  // you must define at least one handler
  import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
  val someHandler: RequestHandler = { case r: HttpRequest => HttpResponse(entity = "Hello!") }

  
  import com.wix.e2e.http.server.WebServerFactory._
  val server = aMockWebServerWith(someHandler).build
  
  // server will start on an open port
  server.start()  
```

Optionally specify port if you want a server on a specific port
```
  val somePort = 6667
  aMockWebServerWith(someHandler).onPort(somePort)
                                 .build
```

##Stub Web Server
Server created in order to respond to requests and record all incoming traffic.
The default behavior would be to respond with 200 ok.
You should use this server if the tested system you are simulating does not return a valid output that will be a part of the server flow and can be validated later on.

Create server
```
  // you must define at least one handler
  import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
  val someHandler: RequestHandler = { case r: HttpRequest => HttpResponse(entity = "Hello!") }


  import com.wix.e2e.http.server.WebServerFactory._
  val server = aStubWebServer.build
  
  // server will start on an open port
  server.start()  
```

On test flow you can check that requests were recieved on the tested system.
```
   val server = aStubWebServer.build
   
   get("/somePath")(server.baseUri)
   
   server.recordedRequests must contain( beGetRequestWith(path = somePath) )
```

Optional customizations
```
  val server = aStubWebServer
  
  // set explicit port
  server.onPort(somePort)
  
  // optinally add handlers
  server.addHandler(someHandler)
  server.addHandlers(anotherHandler, yetAnotherHandler)

  server.start()
```
