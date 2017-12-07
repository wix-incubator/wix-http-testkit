package com.wix.e2e.http.marshaller

import com.wix.e2e.http.api.{Marshaller, NopMarshaller}
import org.specs2.mutable.Spec

import scala.collection.mutable.ListBuffer

class HttpClientMalformedMarshallerContractTest extends Spec {

  "RequestTransformers with malformed marshaller" should {
    "try to create all malformed marshallers and fallback to NopMarshaller when all is failing" in {
      Marshaller.Implicits.marshaller must beAnInstanceOf[NopMarshaller]

      MarshallerCalled.contractorsCalled must containTheSameElementsAs(Seq(classOf[MalformedCustomMarshaller], classOf[MalformedCustomMarshaller2]))
    }
  }
}

class MalformedCustomMarshaller(dummy: Int) extends BaseMalformedCustomMarshaller {
  def this() = {
    this(5)
    markConstractorCalledAndExplode
  }
}

class MalformedCustomMarshaller2(dummy: Int) extends BaseMalformedCustomMarshaller {
  def this() = {
    this(5)
    markConstractorCalledAndExplode
  }
}

abstract class BaseMalformedCustomMarshaller extends Marshaller {
  def markConstractorCalledAndExplode = {
    MarshallerCalled.markConstructorCalled(getClass)
    throw new RuntimeException("whatever")
  }

  def unmarshall[T : Manifest](jsonStr: String): T = ???
  def marshall[T](t: T): String = ???
}

object MarshallerCalled {
  private val called = ListBuffer.empty[Class[_]]

  def markConstructorCalled(clazz: Class[_]) = this.synchronized {
    called.append(clazz)
  }

  def contractorsCalled: Seq[Class[_]] = this.synchronized {
    called
  }
}

