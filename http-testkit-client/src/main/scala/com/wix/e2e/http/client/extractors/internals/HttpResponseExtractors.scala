package com.wix.e2e.http.client.extractors.internals

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.utils.waitFor
import com.wix.e2e.http.{HttpRequest, HttpResponse, WixHttpTestkitResources}

import scala.concurrent.duration._

trait HttpResponseExtractors { self: HttpEntityTransformers =>
  implicit class WithHttpResponseTransformations(response: HttpResponse) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller, atMost: FiniteDuration = 5.seconds): T = response.entity.extractAs[T]
    def extractAsString(implicit atMost: FiniteDuration = 5.seconds): String = response.entity.extractAsString
    def extractAsBytes(implicit atMost: FiniteDuration = 5.seconds): Array[Byte] = response.entity.extractAsBytes
  }
}

trait HttpEntityTransformers {
  implicit class WithHttpResponseEntityTransformations[E <: HttpEntity](entity: E) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller, atMost: FiniteDuration = 5.seconds): T = marshaller.unmarshall[T](extract[String])
    def extractAsString(implicit atMost: FiniteDuration = 5.seconds): String = extract[String]
    def extractAsBytes(implicit atMost: FiniteDuration = 5.seconds): Array[Byte] = extract[Array[Byte]]

    import WixHttpTestkitResources.materializer
    private def extract[T](implicit um: Unmarshaller[E, T], atMost: FiniteDuration = 5.seconds) = waitFor(Unmarshal(entity).to[T])(atMost)
  }
}

trait HttpRequestExtractors { self: HttpEntityTransformers =>
  implicit class WithHttpResponseTransformations(request: HttpRequest) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller, atMost: FiniteDuration = 5.seconds): T = request.entity.extractAs[T]
    def extractAsString(implicit atMost: FiniteDuration = 5.seconds): String = request.entity.extractAsString
    def extractAsBytes(implicit atMost: FiniteDuration = 5.seconds): Array[Byte] = request.entity.extractAsBytes
  }
}
