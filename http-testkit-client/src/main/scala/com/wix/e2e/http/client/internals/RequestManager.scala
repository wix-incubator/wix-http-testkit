package com.wix.e2e.http.client.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{ProductVersion, `Transfer-Encoding`, `User-Agent`}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.stream.StreamTcpException
import com.wix.e2e.http._
import com.wix.e2e.http.exceptions.ConnectionRefusedException
import com.wix.e2e.http.info.HttpTestkitVersion
import com.wix.e2e.http.utils._

import scala.concurrent.Future
import scala.concurrent.duration._


trait RequestManager[R] {
  def apply(path: String, but: RequestTransformer = identity, withTimeout: FiniteDuration = 5.seconds)(implicit baseUri: BaseUri): R
}

class NonBlockingRequestManager(request: HttpRequest) extends RequestManager[Future[HttpResponse]] {


  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): Future[HttpResponse] = {
    val transformed = Seq(composeUrlFor(baseUri, path), but)
                                .foldLeft(request) { case (r, tr) => tr(r) }
    import WixHttpTestkitResources.{executionContext, materializer, system}
    Http().singleRequest(request = transformed,
                         settings = settingsWith(withTimeout))
          .map({ response =>
            if (response.entity.isChunked())
              response.copy(headers = response.headers.map {
                case t: `Transfer-Encoding` => t.withChunked
                case h => h
              })
            else
              response
          })
          .recoverWith( { case _: StreamTcpException => Future.failed(throw new ConnectionRefusedException(baseUri)) } )
  }

  private def composeUrlFor(baseUri: BaseUri, path: String): RequestTransformer =
    _.copy(uri = baseUri.asUriWith(path) )

  private def settingsWith(timeout: FiniteDuration) = {
    val settings = ConnectionPoolSettings(WixHttpTestkitResources.system)
    settings.withConnectionSettings( settings.connectionSettings
                                             .withConnectingTimeout(timeout)
                                             .withIdleTimeout(timeout)
                                             .withUserAgentHeader(Some(`User-Agent`(ProductVersion("client-http-testkit", HttpTestkitVersion)))) )
            .withMaxConnections(32)
            .withPipeliningLimit(4)
            .withMaxRetries(0)
  }
}

class BlockingRequestManager(request: HttpRequest) extends RequestManager[HttpResponse] {

  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): HttpResponse = {
    import WixHttpTestkitResources.{executionContext, materializer}
    
    waitFor(nonBlockingRequestManager(path, but, withTimeout).flatMap(_.toStrict(withTimeout)))(Duration.Inf)
  }

  private val nonBlockingRequestManager = new NonBlockingRequestManager(request)
}
