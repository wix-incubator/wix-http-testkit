#Stub Servers Request Matchers

 
Import the matcher suite

```scala
  import com.wix.e2e.http.matchers.RequestMatchers._

```

You can also use trait mixin

```scala
    class MyTestClass extends SpecWithJUnit with RequestMatchers     
```
 
### Validate Recorded Requests
 
```scala
    val server = aStubWebServer.build
                               .start()
  
    // match concrete requests                          
    val request = HttpRequest(HttpMethods.GET)                               
    server must receivedAnyOf(request)

    // compose matchers
    
    server must receivedAnyRequestThat(must = beGet)
``` 

#Request Matchers

###Method Matchers

All Http request statuses can be matched

```scala
    val request = // server.recordedRequests.head
    
    request must beGet
    request must bePost
    // more method matchers are available
```

### Request URL Matchers

Match against request path or parameters
```scala
    // request path
    request must havePath("/somePath")
    request must havePathThat(must = contain("/somePath"))
     
    // request parameters 
    request must haveAnyParamOf("param1" -> "value1", "param2" -> "value2")
    request must haveAnyParamThat(must = be_===( "value1" ), withParamName = "param1")
```



###Body Matchers

It is possible to match request body in several ways
```scala
    //Match exact content
    request must haveBodyWith(bodyContent = "someBody")
    
    // compose matchers
    request must haveBodyThat(must = contain("someBody"))
```

Unmarshal and match

```scala
    case class SomeCaseClass(s: String)
    
    request must haveBodyWith(entity = SomeCaseClass("some string"))
     
    // or compose matchers 
    request must haveBodyEntityThat(must = be_===( SomeCaseClass("some string") ))
```

All requests are unmarshalled with default or custom marshaller, for more info see [Marshaller Documentation](./MARSHALLER.md)


###Headers Matchers

Check if request contain headers

```scala
    request must haveAnyHeadersOf("h1" -> "v1", "h2" -> "v2") // at least one is found 
    request must haveAllHeadersOf("h1" -> "v1", "h2" -> "v2") // all exists on request 
    request must haveTheSameHeadersAs("h1" -> "v1", "h2" -> "v2") // same list of headers (no more, no less)
     
    // compose
    request must haveAnyHeaderThat(must = contain("value"), withHeaderName = "header" ) 

```

Check if request contain headers cookies
```scala
    request must receivedCookieWith(name = "cookie name")
    
    request must receivedCookieThat(must = be_===( HttpCookie("cookie name", "cookie value") ))

```

##Create Your Own

You can mix and match and create your own [Specs<sup>2</sup> matchers](http://etorreborre.github.io/specs2/), if there are more commonly used matchers you are using and think that should be included do not hasitate to open an issue or create a PR.

