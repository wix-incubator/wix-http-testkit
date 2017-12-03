package com.wix.e2e.http.json


import com.wix.e2e.http.api.Marshaller
import org.specs2.mutable.Spec


class DualMarshallersTest extends Spec {

  "Dual Marshallers" should {
    "pick the custom one over the testkit provided marshaller" in {
      Marshaller.Implicits.marshaller must beAnInstanceOf[DummyCustomMarshaller]
    }
  }
}

class DummyCustomMarshaller extends Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T = ???
  def marshall[T](t: T): String = ???
}
