package com.wix.e2e.http.server.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.settings.ServerSettings
import com.wix.e2e.http.api.BaseWebServer
import com.wix.e2e.http.utils._
import com.wix.e2e.http.{BaseUri, RequestHandler, WixHttpTestkitResources}

abstract class AkkaHttpMockWebServer(specificPort: Option[Int], val initialHandlers: Seq[RequestHandler])
  extends BaseWebServer
  with AdjustableServerBehaviorSupport {

  private implicit val system = WixHttpTestkitResources.system
  private implicit val materializer = WixHttpTestkitResources.materializer

  protected def serverBehavior: RequestHandler

  def start() = this.synchronized {
    val s = waitFor( Http().bindAndHandleSync(handler = serverBehavior,
                                              interface = "localhost",
                                              settings = customSettings,
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
  private def customSettings = ServerSettings(system).withTransparentHeadRequests(false)
}
