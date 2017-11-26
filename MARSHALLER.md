# Json Marshaller

Testkit comes out of the box with a default [Jackson](https://github.com/FasterXML/jackson) json marshaller preloaded with ([Scala Module](https://github.com/FasterXML/jackson-module-scala), [JDK8](https://github.com/FasterXML/jackson-datatype-jdk8), [Java Time](https://github.com/FasterXML/jackson-datatype-jsr310), [JodaTime](https://github.com/FasterXML/jackson-datatype-joda))

It can also allow you to create your own custom marshaller: 

```scala

    val myMarshaller = new com.wix.e2e.http.api.Marshaller {
      def unmarshall[T : Manifest](jsonStr: String): T = { /*your code here*/ }
      def marshall[T](t: T): String = { /*your code here*/ }
    }

    
    // on call site, define implicit marshaller
    implicit val customMarshaller = myMarshaller 

    put("/somePath", apply = withPayload(SomeCaseClass("Hi There !!!")))
    
```
