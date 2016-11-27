package com.wix.hoopoe.http.client.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import com.wix.hoopoe.http._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


trait RequestManager[R] {
  def apply(path: String, but: RequestTransformer = identity, withTimeout: FiniteDuration = 5.seconds)(implicit baseUri: BaseUri): R
}

class NonBlockingRequestManager(request: HttpRequest) extends RequestManager[Future[HttpResponse]] {

  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): Future[HttpResponse] = {
    val transformed = Seq(composeUrlFor(baseUri, path), but)
                                .foldLeft(request) { case (r, tr) => tr(r) }
    Http(WixHttpTestkitResources.system).singleRequest(request = transformed)(WixHttpTestkitResources.materializer)
  }

  private def composeUrlFor(baseUri: BaseUri, withPath: String): RequestTransformer =
    _.copy(uri = Uri(s"http://localhost:${baseUri.port}/$withPath"))
}

class BlockingRequestManager(request: HttpRequest) extends RequestManager[HttpResponse] {

  def apply(path: String, but: RequestTransformer, withTimeout: FiniteDuration)(implicit baseUri: BaseUri): HttpResponse =
    Await.result( nonBlockingRequestManager(path, but, withTimeout), Duration.Inf)

  private val nonBlockingRequestManager = new NonBlockingRequestManager(request)
}
