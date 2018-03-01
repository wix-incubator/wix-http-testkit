package com.wix.e2e.http.filters

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.{Empty, Segment, Slash}
import com.wix.e2e.http.RequestFilter

trait PathFilters {

  def forPath(path: String): RequestFilter = { rq =>
    val expectedPath = toList(Path(path))
    val requestedPath = toList(rq.uri.path)

    matchPath(expectedPath, requestedPath)
  }

  private def matchPath(expected: List[Segment], actual: List[Segment]): Boolean = {
    if (expected.isEmpty && actual.isEmpty) true
    else if (expected.isEmpty || actual.isEmpty) false
    else expected.head.head match {
      case "*"     => matchPath(expected.tail, actual.tail)
      case "**"    => matchPath(expected, actual.tail) || matchPath(expected.tail, actual.tail)
      case segment => segment == actual.head.head && matchPath(expected.tail, actual.tail)
    }
  }

  private def toList(path: Path): List[Segment] = path match {
    case Empty => Nil
    case _: Slash => toList(path.tail)
    case s: Segment => s :: toList(path.tail)
  }
}
