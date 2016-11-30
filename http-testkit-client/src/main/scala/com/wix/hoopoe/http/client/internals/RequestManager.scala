package com.wix.hoopoe.http.client.internals

import akka.http.scaladsl.Http
import akka.stream.StreamTcpException
import com.wix.hoopoe.http._
import com.wix.hoopoe.http.exceptions.ConnectionRefusedException
import com.wix.hoopoe.http.utils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


trait RequestManager[R] {
  def apply(path: String, but: RequestTransformer = identity, withTimeout: FiniteDuration = 5.seconds)(implicit baseUri: BaseUri): R
}

class NonBlockingRequestManager(request: HttpRequest) extends RequestManager[Future[HttpResponse]] {


  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): Future[HttpResponse] = {
    val transformed = Seq(composeUrlFor(baseUri, path), but)
                                .foldLeft(request) { case (r, tr) => tr(r) }
    import WixHttpTestkitResources.{materializer, system}
    Http().singleRequest(request = transformed)
          .recoverWith( { case _: StreamTcpException => Future.failed(throw new ConnectionRefusedException(baseUri)) } )
          .withTimeoutOf(duration = withTimeout)
  }

  private def composeUrlFor(baseUri: BaseUri, path: String): RequestTransformer =
    _.copy(uri = baseUri.asUriWith(path) )
}

class BlockingRequestManager(request: HttpRequest) extends RequestManager[HttpResponse] {

  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): HttpResponse =
    waitFor( nonBlockingRequestManager(path, but, withTimeout) )

  private val nonBlockingRequestManager = new NonBlockingRequestManager(request)
}
