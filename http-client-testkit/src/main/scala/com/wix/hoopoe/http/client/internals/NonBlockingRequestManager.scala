package com.wix.hoopoe.http.client.internals

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.RequestTransformer
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import com.wix.e2e.BaseUri

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class NonBlockingRequestManager(request: HttpRequest) {

  def apply(path: String, but: RequestTransformer = identity, withTimeout: FiniteDuration = 5.seconds)(implicit baseUri: BaseUri): Future[HttpResponse] = {
    val transformed = Seq(composeUrlFor(baseUri, path), but)
                                .foldLeft(request) { case (r, tr) => tr(r) }
    Http(AkkaClientResources.system).singleRequest(request = transformed)(AkkaClientResources.materializer)
  }

  private def composeUrlFor(baseUri: BaseUri, withPath: String): RequestTransformer =
    _.copy(uri = Uri(s"http://localhost:${baseUri.port}/$withPath"))
}

class BlockingRequestManager(request: HttpRequest) {

  private val nonBlockingRequestManager = new NonBlockingRequestManager(request)

  def apply(path: String, but: RequestTransformer = identity, withTimeout: FiniteDuration = 5.seconds)
           (implicit baseUri: BaseUri): HttpResponse =
    Await.result( nonBlockingRequestManager(path, but, withTimeout), Duration.Inf)

}

object AkkaClientResources {
  implicit val system = ActorSystem("http-testkit-client")
  implicit val materializer = ActorMaterializer()
}

