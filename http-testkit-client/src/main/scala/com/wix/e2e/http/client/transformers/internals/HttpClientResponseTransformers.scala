package com.wix.e2e.http.client.transformers.internals

import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.HttpResponse
import com.wix.e2e.http.WixHttpTestkitResources.materializer
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.utils.waitFor

trait HttpClientResponseTransformers {
  implicit class WithHttpResponseTransformations(response: HttpResponse) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller): T = marshaller.unmarshall[T](asString)

    def asString: String = waitFor(Unmarshal(response.entity).to[String])

    def asBytes: Array[Byte] = waitFor(Unmarshal(response.entity).to[Array[Byte]])
  }
}
