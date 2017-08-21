package com.wix.e2e.http.drivers

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.wix.e2e.http.drivers.MarshallingTestObjects.SomeCaseClass
import com.wix.e2e.http.exceptions.MissingMarshallerException
import com.wix.test.random.{randomInt, randomStr}
import org.specs2.execute.AsResult
import org.specs2.matcher.Matcher
import org.specs2.matcher.ResultMatchers.beError

trait MarshallerTestSupport {
  val someObject = SomeCaseClass(randomStr, randomInt)
  val content = randomStr

  def aResponseWith(body: String) = HttpResponse(entity = body)
  def aRequestWith(body: String) = HttpRequest(entity = body)
  val request = HttpRequest()
}


object MarshallingTestObjects {
  case class SomeCaseClass(s: String, i: Int)
}

object MarshallerMatchers {
  def beMissingMarshallerMatcherError[T : AsResult]: Matcher[T] = beError[T](new MissingMarshallerException().getMessage)
}
