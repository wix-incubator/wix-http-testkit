[![Build Status](https://travis-ci.org/wix/wix-http-testkit.svg?branch=master)](https://travis-ci.org/wix/wix-http-testkit)
[![Maven Central (2.12)](https://maven-badges.herokuapp.com/maven-central/com.wix/http-testkit_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.wix/http-testkit_2.12)
[![Maven Central (2.11)](https://maven-badges.herokuapp.com/maven-central/com.wix/http-testkit_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.wix/http-testkit_2.11)

# HTTP Testkit

Overview
========

Wix Http Testkit is a library that will address many of the End-to-end testing concerns you might encounter.

Wix HTTP Testkit aims to be:
* __Simple__ Testing REST services or starting mock/stub servers is very simple and requires very few lines of code.
* __Fast__ Leveraging [Akka-Http](https://github.com/akka/akka-http) infrastructure, starting servers takes milliseconds.
* __Integrated__: Other than providing a set of DSLs to support composing and executing REST HTTP calls and creating and configuring web servers, it also contains out of the box matcher libraries for [Specs<sup>2</sup>](http://wix.github.io/accord/specs2.html) to easily validate each aspect of the tested flow.


Getting Started
===============
### Testing Client

Import DSL
```scala
import com.wix.e2e.http.client.sync._
```

Issue Call
```scala
    val somePort = 99123 /// any port
    implicit val baseUri = BaseUri(port = somePort)


    get("/somePath", 
        but = withParam("param1" -> "value") 
          and header("header" -> "value") 
          and withCookie("cookie" -> "cookieValue"))
```

Use Specs2 Matcher suite to match response
```scala
    import com.wix.e2e.http.matchers.ResponseMatchers._

    put("/anotherPath") must haveBodyWith("someBody")
```

For more info see [Http Client Documentation](./HTTP_CLIENT.md) and [Response Matchers Suite](./HTTP_CLIENT_MATCHERS.md).


### Web Servers

Import Factory
```scala
    import com.wix.e2e.http.server.WebServerFactory._
```

Run an easily programmable web server

```scala
    val handler: RequestHandler = { case r: HttpRequest => HttpResponse()  }
    val server = aMockWebServerWith(handler).build
                                            .start()
```

Or run a programmable that will record all incoming messages

```scala
    val server = aStubWebServer.build
                               .start()

```

Match against recorded requests

```scala

  import com.wix.e2e.http.matchers.RequestMatchers._
  
  
  server must receivedAnyRequestThat(must = beGet)
```

For more info see [Web Server Documentation](./WEBSERVER.md) and [Request Matchers Suite](./WEBSERVER_MATCHERS.md).



## Usage 

HTTP-testkit version '0.1.10' is available on Maven Central Repository. Scala versions 2.11.x and 2.12.x are supported.

### SBT
Simply add the *wix-http-testkit* module to your build settings:

```sbt
libraryDependencies += "com.wix" %% "http-testkit" % "0.1.10"
```
### Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>http-testkit_${scala.tools.version}</artifactId>
    <version>0.1.10</version>
  </dependency>
</dependencies>

```

# Documentation 

* __Rest Client__: a declarative REST client [Documentation](./HTTP_CLIENT.md).  
* __Simplicator Web Servers__: Easily configurable web servers [Documentation](./WEBSERVER.md).
* __Specs<sup>2</sup> Matchers Suite__: Comprehensive matcher suites [Response Matchers](./HTTP_CLIENT_MATCHERS.md) and [Request Matchers](./WEBSERVER_MATCHERS.md).    

# Contribute

Ideas and feature requests welcome! Report an [issue](https://github.com/wix/wix-http-testkit/issues/) or contact the [maintainer](https://github.com/noam-almog) directly.


## License

This project is licensed under [MIT License](./LICENSE.md).
