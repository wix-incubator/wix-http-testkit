package com.wix.e2e.http.client.internals

import org.specs2.mutable.Specification
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

class BlockingRequestManagerTest extends Specification {
  "transformException" should {
    "replace stack" in {
      val exception = anExceptionWithWrongStack(new RuntimeException)
      val localStack = Thread.currentThread().getStackTrace.toSeq.drop(2)
      val throwable = BlockingRequestManager.transformException(exception)
      throwable.getStackTrace.toSeq.mkString("\n") must endingWith(localStack.mkString("\n"))
    }
    "attach suppressed OriginalExceptionStack, if exception with stack " in {
      val exception = anExceptionWithWrongStack(new RuntimeException)
      val originalStackTrace = exception.getStackTrace
      val throwable = BlockingRequestManager.transformException(exception)
      throwable.getSuppressed.toSeq must contain(like[Throwable] {
        case e => e.getStackTrace.toSeq must_=== originalStackTrace.toSeq
      })
    }
    "not attach suppressed OriginalExceptionStack, if exception with NO stack " in {
      val exception = anExceptionWithWrongStack(new RuntimeException)
      exception.setStackTrace(Array.empty)
      val throwable = BlockingRequestManager.transformException(exception)
      throwable.getSuppressed must beEmpty
    }
  }

  private def anExceptionWithWrongStack(create: => Throwable): Throwable = {
    Await.result(Future { create }, 100.millis)
  }


}
