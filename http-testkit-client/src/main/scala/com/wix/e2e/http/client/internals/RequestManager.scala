package com.wix.e2e.http.client.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.TransferEncodings.chunked
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
          .map( recreateTransferEncodingHeader )
          .flatMap( _.toStrict(withTimeout) )
          .recoverWith( { case _: StreamTcpException => Future.failed(new ConnectionRefusedException(baseUri)) } )
  }

  private def composeUrlFor(baseUri: BaseUri, path: String): RequestTransformer =
    _.copy(uri = baseUri.asUriWith(path) )

  private def recreateTransferEncodingHeader(r: HttpResponse) =
    if ( !r.entity.isChunked ) r
    else {
      val encodings = r.header[`Transfer-Encoding`]
                       .map( _.encodings )
                       .getOrElse( Seq.empty )
      r.removeHeader("Transfer-Encoding")
       .addHeader(`Transfer-Encoding`(chunked, encodings:_*))
    }

  private def settingsWith(timeout: FiniteDuration) = {
    val settings = ConnectionPoolSettings(WixHttpTestkitResources.system)
    settings.withConnectionSettings( settings.connectionSettings
                                             .withIdleTimeout(timeout)
                                             .withUserAgentHeader(Some(`User-Agent`(ProductVersion("client-http-testkit", HttpTestkitVersion))))
                                             .withConnectingTimeout(timeout)
                                             .withParserSettings( settings.connectionSettings
                                                                          .parserSettings //maxHeaderValueLength
                                                                          .withMaxHeaderValueLength(32 * 1024) ) )
            .withMaxConnections(32)
            .withPipeliningLimit(4)
            .withMaxRetries(0)
  }
}

class BlockingRequestManager(request: HttpRequest) extends RequestManager[HttpResponse] {

  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): HttpResponse =
    waitFor(nonBlockingRequestManager(path, but, withTimeout))(withTimeout + 1.second)

  private val nonBlockingRequestManager = new NonBlockingRequestManager(request)
}
