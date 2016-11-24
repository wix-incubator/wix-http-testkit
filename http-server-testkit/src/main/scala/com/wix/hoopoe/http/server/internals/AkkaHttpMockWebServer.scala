package com.wix.hoopoe.http.server.internals

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.wix.e2e.BaseUri
import com.wix.hoopoe.http.server.BaseWebServer
import com.wix.hoopoe.http.server.WebServerFactory.RequestHandler
import com.wix.hoopoe.http.server.exceptions.PortUnknownYetException

import scala.concurrent.Await
import scala.concurrent.duration._

abstract class AkkaHttpMockWebServer(specificPort: Option[Int]) extends BaseWebServer {
  implicit val system = AkkaServerResources.system
  implicit val materializer = AkkaServerResources.materializer

  protected def serverBehavior: RequestHandler

  def start() = {
    val s = Await.result( Http().bindAndHandleSync(serverBehavior, "localhost", specificPort.getOrElse( AllocateDynamicPort )), 5.seconds )
    serverBinding = Option(s)
  }

  def stop() = {
    serverBinding.foreach{ s =>
      Await.result( s.unbind(), 5.seconds )
    }
    serverBinding = None
  }

  def baseUri = specificPort.map( p => BaseUri("localhost", port = p) )
                            .orElse( serverBinding.map( s => BaseUri(port = s.localAddress.getPort) ))
                            .getOrElse( throw new PortUnknownYetException )

  private var serverBinding: Option[ServerBinding] = None
  private val AllocateDynamicPort = 0
}

object AkkaServerResources {
  implicit val system = ActorSystem("http-testkit-server")
  implicit val materializer = ActorMaterializer()
}






