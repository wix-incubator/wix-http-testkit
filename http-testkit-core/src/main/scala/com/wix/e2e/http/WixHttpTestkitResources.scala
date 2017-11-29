package com.wix.e2e.http

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.xml.PrettyPrinter

object WixHttpTestkitResources {
  implicit val system = ActorSystem("wix-http-testkit")
  implicit val materializer = ActorMaterializer()
  private val threadPool = Executors.newCachedThreadPool
  implicit val executionContext = ExecutionContext.fromExecutor(threadPool)

  def xmlPrinter = new PrettyPrinter(80, 2)

  sys.addShutdownHook {
    system.terminate()
    materializer.shutdown()
    threadPool.shutdown()
  }
}