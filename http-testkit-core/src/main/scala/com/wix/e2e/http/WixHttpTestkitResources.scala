package com.wix.e2e.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.xml.PrettyPrinter

object WixHttpTestkitResources {
  implicit val system = ActorSystem("wix-http-testkit")
  implicit val materializer = ActorMaterializer()

  lazy val xmlPrinter = new PrettyPrinter(80, 2)
}