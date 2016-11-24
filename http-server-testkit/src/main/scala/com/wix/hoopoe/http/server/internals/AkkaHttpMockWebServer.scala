package com.wix.hoopoe.http.server.internals

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.wix.e2e.BaseUri
import com.wix.hoopoe.http.server.BaseWebServer
import com.wix.hoopoe.http.server.WebServerFactory.RequestHandler
import com.wix.hoopoe.http.server.exceptions.PortUnknownYetException
import com.wix.hoopoe.http.server.utils._

abstract class AkkaHttpMockWebServer(specificPort: Option[Int]) extends BaseWebServer {
  implicit val system = AkkaServerResources.system
  implicit val materializer = AkkaServerResources.materializer

  protected def serverBehavior: RequestHandler

  def start() = {
    val s = waitFor( Http().bindAndHandleSync(handler = serverBehavior,
                                              interface = "localhost",
                                              port = specificPort.getOrElse( AllocateDynamicPort )) )
    serverBinding = Option(s)
    println(s"Web server started on port: ${baseUri.port}.")
  }

  def stop() = {
    serverBinding.foreach{ s =>
      waitFor( s.unbind() )
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






