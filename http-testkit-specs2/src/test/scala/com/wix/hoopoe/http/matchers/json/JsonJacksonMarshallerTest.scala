package com.wix.hoopoe.http.matchers.json

import java.time.LocalDateTime
import java.util.Optional

import com.wix.hoopoe.http.matchers.json.MarshallingTestObjects.SomeCaseClass
import com.wixpress.hoopoe.test._
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class JsonJacksonMarshallerTest extends SpecWithJUnit {

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
  }
}

object MarshallingTestObjects {
  case class SomeCaseClass(s: String, i: Int)
}
