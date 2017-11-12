package com.wix.e2e.http.client.transformers.internals

import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import com.wix.e2e.http.HttpResponse
import com.wix.e2e.http.WixHttpTestkitResources.materializer
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.utils.waitFor

trait HttpClientResponseTransformers {
  implicit class WithHttpResponseTransformations(response: HttpResponse) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller): T = marshaller.unmarshall[T](extract[String])
    def extractAsString: String = extract[String]
    def extractAsBytes: Array[Byte] = extract[Array[Byte]]

    private def extract[T](implicit um: Unmarshaller[ResponseEntity, T]) = waitFor(Unmarshal(response.entity).to[T])
  }

}
