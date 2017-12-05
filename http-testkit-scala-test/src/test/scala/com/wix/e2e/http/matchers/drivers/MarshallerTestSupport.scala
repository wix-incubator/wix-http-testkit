package com.wix.e2e.http.matchers.drivers

import com.wix.e2e.http.api.Marshaller
import org.scalatest.jmock.JMockCycle

//import MockFactory._
//import org.specs2.matcher.ThrownExpectations
//import org.specs2.mock.Mockito

trait MarshallerTestSupport /*with ThrownExpectations */ {
  private val cycle = new JMockCycle
//  val marshaller: Marshaller = mock[Marshaller]

  // todo: fix mocks !!!
  def givenUnmarshallerWith[T : Manifest](someEntity: T, forContent: String) =
    new Marshaller {
      def unmarshall[T: Manifest](jsonStr: String): T = someEntity.asInstanceOf[T]
      def marshall[T](t: T): String = ???
    }

//    expecting { e => import e._
////      when(marshaller.unmarshall[T](forContent)).then
////      when(marshaller.unmarshall/*[T]*/(withArg(forContent))).thenReturn(someEntity)
//      oneOf(marshaller.unmarshall[T](forContent)); will(returnValue(someEntity))
////      marshaller.unmarshall[T](withArg(forContent)); //.thenReturn(someEntity)
//
////         oneOf (mockCollaborator).documentAdded("Document")
////         exactly(3).of (mockCollaborator).documentChanged("Document")
//       }

//    whenExecuting { case e =>
////      e.on
//      marshaller.unmarshall[T](forContent)//.thenReturn(someEntity)
//    }
//    when(marshaller.unmarshall[T](forContent)).thenReturn(someEntity)

  def givenBadlyBehavingUnmarshallerFor[T : Manifest](withContent: String) = {
    new Marshaller {
      def unmarshall[T: Manifest](jsonStr: String): T = throw new RuntimeException
      def marshall[T](t: T): String = throw new RuntimeException
    }
//    when(marshaller.unmarshall[T](withContent)).thenThrow(new RuntimeException)
//    marshaller.unmarshall[T](withContent) throws new RuntimeException
  }
}

trait CustomMarshallerProvider {
  def marshaller: Marshaller
  implicit def customMarshaller: Marshaller = marshaller
}

