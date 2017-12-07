package com.wix.e2e.http.client.extractors

import akka.http.scaladsl.model.{HttpEntity, HttpMessage}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.wix.e2e.http.WixHttpTestkitResources
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.utils.waitFor

import scala.concurrent.duration._

trait HttpMessageExtractors {
  implicit class HttpMessageExtractorsOps[M <: HttpMessage](message: M) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller, atMost: FiniteDuration = 5.seconds): T = message.entity.extractAs[T]
    def extractAsString(implicit atMost: FiniteDuration = 5.seconds): String = message.entity.extractAsString
    def extractAsBytes(implicit atMost: FiniteDuration = 5.seconds): Array[Byte] = message.entity.extractAsBytes
  }

  implicit class HttpEntityExtractorsOps[E <: HttpEntity](entity: E) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller, atMost: FiniteDuration = 5.seconds): T = marshaller.unmarshall[T](extract[String])
    def extractAsString(implicit atMost: FiniteDuration = 5.seconds): String = extract[String]
    def extractAsBytes(implicit atMost: FiniteDuration = 5.seconds): Array[Byte] = extract[Array[Byte]]

    import WixHttpTestkitResources.materializer
    private def extract[T](implicit um: Unmarshaller[E, T], atMost: FiniteDuration) = waitFor(Unmarshal(entity).to[T])(atMost)
  }
}

object HttpMessageExtractors extends HttpMessageExtractors