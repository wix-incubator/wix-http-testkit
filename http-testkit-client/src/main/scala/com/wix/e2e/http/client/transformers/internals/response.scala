package com.wix.e2e.http.client.transformers.internals

import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.HttpResponse
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.utils.waitFor

import scala.concurrent.ExecutionContext

trait HttpClientResponseTransformers {
  implicit class `HttpResponse --> T`(r: HttpResponse) {
    def extractAs[T : Manifest](implicit marshaller: Marshaller): T =
      marshaller.unmarshall[T]( httpRequestAsString )

    import com.wix.e2e.http.WixHttpTestkitResources.materializer

    import ExecutionContext.Implicits.global
    private def httpRequestAsString = waitFor( Unmarshal(r.entity).to[String] )
  }
}