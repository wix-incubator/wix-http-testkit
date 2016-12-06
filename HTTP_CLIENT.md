Overview
========

A sane DSL to test REST API's.

There are two variations of the client that can be used:
* __Blocking__
* __Non-Blocking__


##Create HTTP Client
 
####Import the DSL: 

package import:
```scala
    
    // blocking implementation
    import com.wix.e2e.http.client.sync._
    
    // or non blocking implementation
    import com.wix.e2e.http.client.async._
```

Import Object:
```scala

    // blocking implementation
    import com.wix.e2e.http.client.BlockingHttpClientSupport
    
    // or non blocking implementation
    import com.wix.e2e.http.client.NonBlockingHttpClientSupport

```

Or add mixin trait to call site

```scala

    // blocking implementation
    class MyClass extends com.wix.e2e.http.client.BlockingHttpClientSupport
     
    // or non blocking implementation
    class MyClass extends com.wix.e2e.http.client.NonBlockingHttpClientSupport 
```

####Issuing New Request
```scala

    val somePort = 99123 /// any port
    implicit val baseUri = BaseUri(port = somePort)

    get("/somePath")
    post("/anotherPath")
    // suported method: get, post, put, patch, delete, options, head, trace
```

### Customizing Request

Each request can be easily customized with a set of basic transformers allowing all basic functionality (add parameters, headers, cookies and request body)
```scala

    get("/somePath", 
        but = withParam("param1" -> "value") 
          and header("header" -> "value") 
          and withCookie("cookie" -> "cookieValue"))
          
    // post plain text data to api
    post("/somePath", 
         but = withPayload("Hi There !!!"))
    
    // or post entity that would be marshalled using testkit marshaller (or custom user marshaller)
    case class SomeCaseClass(str: String)
    
    // request will automatically be marshalled to json
    put("/somePath", but = withPayload(SomeCaseClass("Hi There !!!")))
```

Handlers can be also be defined by developer, it can use existing transformers or to implement transformers from scratch
```scala

    def withSiteId(id: String): RequestTransformer = withParam("site-id" -> id) and withHeader("x-user-custom" -> "whatever")
     
    get("/path", but = withSiteId("someId"))

```

### Validate Responses
To validate HTTP response use the included [Specs<sup>2</sup> Matcher Suite](./HTTP_CLIENT_MATCHERS.md).

### Json Marshaller

Testkit comes out of the box with a default [Jackson](https://github.com/FasterXML/jackson) json marshaller preloaded with several commonly used modules, to define your own marshaller see [Custom Marshaller](./MARSHALLER.md).
