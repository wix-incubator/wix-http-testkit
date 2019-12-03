package com.wix.e2e.http

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.SystemMaterializer
import com.wix.e2e.http.utils._

import scala.concurrent.ExecutionContext
import scala.xml.PrettyPrinter

object WixHttpTestkitResources {
  implicit val system = ActorSystem("wix-http-testkit")
  implicit val materializer = SystemMaterializer.get(system).materializer
  private val threadPool = Executors.newCachedThreadPool
  implicit val executionContext = ExecutionContext.fromExecutor(threadPool)

  system.registerOnTermination {
    threadPool.shutdownNow()
  }

  def xmlPrinter = new PrettyPrinter(80, 2)

  sys.addShutdownHook {
    system.terminate()
    waitFor(system.whenTerminated)
  }
}