package com.wix.hoopoe.http.server.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import com.wix.hoopoe.http.api.BaseWebServer
import com.wix.hoopoe.http.utils._
import com.wix.hoopoe.http.{BaseUri, RequestHandler, WixHttpTestkitResources}

abstract class AkkaHttpMockWebServer(specificPort: Option[Int]) extends BaseWebServer {
  private implicit val system = WixHttpTestkitResources.system
  private implicit val materializer = WixHttpTestkitResources.materializer

  protected def serverBehavior: RequestHandler

  def start() = this.synchronized {
    val s = waitFor( Http().bindAndHandleSync(handler = serverBehavior,
                                              interface = "localhost",
                                              port = specificPort.getOrElse( AllocateDynamicPort )) )
    serverBinding = Option(s)
    println(s"Web server started on port: ${baseUri.port}.")
    this
  }

  def stop() = this.synchronized {
    serverBinding.foreach{ s =>
      waitFor( s.unbind() )
    }
    serverBinding = None
    this
  }

  def baseUri =
    specificPort.map( p => BaseUri("localhost", port = p) )
                .orElse( serverBinding.map( s => BaseUri(port = s.localAddress.getPort) ))
                .getOrElse( throw new IllegalStateException("Server port and baseUri will have value after server is started") )

  private var serverBinding: Option[ServerBinding] = None
  private val AllocateDynamicPort = 0
}
