# Response Matchers
 

 
Import the matcher suite

```scala
  import com.wix.e2e.http.matchers.ResponseMatchers._

```

You can also use trait mixin

```scala
    class MyTestClass extends SpecWithJUnit with ResponseMatchers     
```
 

### Status Matchers

All Http response statuses can be matched

```scala
    val response = get("/somePath")
    
    response must beSuccessful
    response must beNotFound
    // more statuses are available
```

### Body Matchers

It is possible to match response body in several ways
```scala
    //Match exact content
    response must haveBodyWith("someBody")
    
    // compose matchers
    response must haveBodyThat(must = contain("someBody"))
```

Unmarshal and match

```scala
    case class SomeCaseClass(s: String)
    
    response must haveBodyWith(SomeCaseClass("some string"))
     
    // or compose matchers 
    response must haveBodyThat(must = be_===( SomeCaseClass("some string") ))
```

All responses are unmarshalled with default or custom marshaller, for more info see [Marshaller Documentation](./MARSHALLER.md)


### Headers Matchers

Check if response contain headers

```scala
    response must haveAnyHeadersOf("h1" -> "v1", "h2" -> "v2") // at least one is found 
    response must haveAllHeadersOf("h1" -> "v1", "h2" -> "v2") // all exists on response 
    response must haveTheSameHeadersAs("h1" -> "v1", "h2" -> "v2") // same list of headers (no more, no less)
     
    // compose
    response must haveAnyHeaderThat(must = contain("value"), withHeaderName = "header" ) 

```

Check if response contain headers cookies
```scala
    response must receivedCookieWith(name = "cookie name")
    
    response must receivedCookieThat(must = be_===( HttpCookie("cookie name", "cookie value") ))

```

### Common Matchers
```scala
    
    // successful response with body
    response must beSuccessfulWith( "some content" )
    response must beSuccessfulWithEntityThat(must = be_===( SomeCaseClass("some content" ) ) )
    
    // more matchers exists

```

## Create Your Own

You can mix and match and create your own [Specs<sup>2</sup> matchers](http://etorreborre.github.io/specs2/), if there are more commonly used matchers you are using and think that should be included do not hasitate to open an issue or create a PR.

