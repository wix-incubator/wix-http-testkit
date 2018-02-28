package com.wix.e2e.http.matchers

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.{Empty, Segment, Slash}
import com.wix.e2e.http.RequestMatcher

object havePath {

  def apply(path: String): RequestMatcher = { rq =>
    val expectedPath = toList(Path(path))
    val requestedPath = toList(rq.uri.path)
    val matchSegments: ((Segment, Segment)) => Boolean = { case (expectedSegment, requestedSegment) ⇒
      expectedSegment.head == "*" ||
        expectedSegment.head == requestedSegment.head
    }

    expectedPath.lengthCompare(requestedPath.length) == 0 &&
      expectedPath.zip(requestedPath).forall(matchSegments)
  }

  private def toList(path: Path): List[Segment] = path match {
    case Empty => Nil
    case _: Slash => toList(path.tail)
    case s: Segment => s :: toList(path.tail)
  }
}
