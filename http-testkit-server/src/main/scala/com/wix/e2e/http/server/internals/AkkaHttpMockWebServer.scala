package com.wix.e2e.http.server.internals

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.{ProductVersion, Server}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ServerSettings
import com.wix.e2e.http.api.BaseWebServer
import com.wix.e2e.http.info.HttpTestkitVersion
import com.wix.e2e.http.utils._
import com.wix.e2e.http.{BaseUri, RequestHandler, WixHttpTestkitResources}

import scala.concurrent.Future
import scala.concurrent.duration._

abstract class AkkaHttpMockWebServer(specificPort: Option[Int], val initialHandlers: Seq[RequestHandler])
  extends BaseWebServer
  with AdjustableServerBehaviorSupport {

  import WixHttpTestkitResources.{executionContext, materializer, system}

  protected def serverBehavior: RequestHandler

  def start() = this.synchronized {
    val s = waitFor( Http().newServerAt("localhost",
                                        port = specificPort.getOrElse( AllocateDynamicPort ))
                           .withSettings( customSettings )
                           .bind(TransformToStrictAndHandle) )
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
  private val TransformToStrictAndHandle: HttpRequest => Future[HttpResponse] = _.toStrict(1.minutes).map( serverBehavior )
  private def customSettings = {
    val settings = ServerSettings(system)
    settings.withTransparentHeadRequests(false)
            .withParserSettings( settings.parserSettings
                                         .withMaxHeaderValueLength(32 * 1024) )
            .withServerHeader( Some(Server(ProductVersion("server-http-testkit", HttpTestkitVersion))) )
  }
}
