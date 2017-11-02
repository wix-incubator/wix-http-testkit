package com.wix.e2e.http.json

import java.time.LocalDateTime
import java.util.Optional

import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.json.MarshallingTestObjects.SomeCaseClass
import com.wix.test.random._
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class JsonJacksonMarshallerTest extends Spec {

  trait ctx extends Scope {
    val someStr = randomStr
    val javaDateTime = LocalDateTime.now()
    val someCaseClass = SomeCaseClass(randomStr, randomInt)
    val dateTime = new DateTime
    val dateTimeUTC = new DateTime(UTC)

    val marshaller: Marshaller = new JsonJacksonMarshaller
  }


  "JsonJacksonMarshaller" should {

    "marshall scala option properly" in new ctx {
      marshaller.unmarshall[Option[String]](
        marshaller.marshall( Some(someStr) )
      ) must beSome(someStr)
    }

    "marshall scala case classes properly" in new ctx {
      marshaller.unmarshall[SomeCaseClass](
        marshaller.marshall( someCaseClass )
      ) must_=== someCaseClass
    }

    "marshall datetime without zone" in new ctx {
      marshaller.unmarshall[DateTime](
        marshaller.marshall( dateTime.withZone(DateTimeZone.getDefault) )
      ) must_=== dateTime.withZone(UTC)
    }

    "marshall date time to textual format in UTC" in new ctx {
      marshaller.marshall( dateTime ) must contain(dateTime.withZone(UTC).toString)
    }


    "marshall java.time objects" in new ctx {
      marshaller.unmarshall[LocalDateTime](
        marshaller.marshall( javaDateTime )
      ) must_=== javaDateTime

    }

    "marshall java 8 Optional" in new ctx {
      marshaller.unmarshall[Optional[DateTime]](
        marshaller.marshall( dateTimeUTC )
      ) must_=== Optional.of(dateTimeUTC)

      marshaller.unmarshall[Optional[SomeCaseClass]](
        marshaller.marshall( someCaseClass )
      ) must_=== Optional.of(someCaseClass)
    }

    "expose jackson object mapper to allow external configuration" in new ctx {
      marshaller.asInstanceOf[JsonJacksonMarshaller].configure must beAnInstanceOf[ObjectMapper]
    }
  }
}

object MarshallingTestObjects {
  case class SomeCaseClass(s: String, i: Int)
}
