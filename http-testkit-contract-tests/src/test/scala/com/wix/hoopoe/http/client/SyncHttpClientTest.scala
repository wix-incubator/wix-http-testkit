package com.wix.hoopoe.http.client

import akka.http.scaladsl.client.RequestBuilding.RequestTransformer
import akka.http.scaladsl.model.headers.RawHeader
import com.wix.hoopoe.http.client.StubWebServerMatchers._
import com.wix.hoopoe.http.client.sync._
import com.wix.hoopoe.http.server.WebServerFactory.aStubWebServer
import com.wixpress.hoopoe.test._
import org.specs2.mutable.{After, SpecWithJUnit}
import org.specs2.specification.Scope


class SyncHttpClientTest extends SpecWithJUnit {


  trait ctx extends Scope with StubWebServerProvider

  "ASyncHttpClient" should {

    "support generating async get, post, put, patch, delete, options and trace request" in new ctx {
      Seq(get -> "Get", post -> "Post", put -> "Put",
          patch -> "Patch", delete -> "Delete",
          options -> "Options"/*, head -> "Head"*/, trace -> "Trace")
        .foreach { case (method, methodName) =>

          val path = s"$methodName/$randomStr"
          server.clearRecordedRequests()

          method(path)

          server must receivedRequestWith(methodName, toPath = path)
        }
    }

//    "throw timeout if response takes more than default timeout" in {
//      val server = aStubWebServer.addHandler( { case _ => Thread.sleep(100); HttpResponse() } )
//        .build
//      server.start()
//
//      Await.result( get("/somePath", withTimeout = 5.millis)(server.baseUri), Duration.Inf) must throwA[AskTimeoutException]
//
//      server.stop()
//      ok
//    }.pendingUntilFixed("find how to define read timeout")


    "allow to customize request using requrest transformers" in new ctx {
      def withHeaders(headers: (String, String)*): RequestTransformer = _.withHeaders( headers.map { case (k, v) => RawHeader(k, v)}:_* )

      get("/somePath", but = withHeaders("h1" -> "v1") )

      server must receivedRequestWith("h1" -> "v1")
    }
  }
}


trait StubWebServerProvider extends After {
  val server = aStubWebServer.build
                             .start()

  def after = server.stop()

  lazy implicit val baseUri = server.baseUri
}
