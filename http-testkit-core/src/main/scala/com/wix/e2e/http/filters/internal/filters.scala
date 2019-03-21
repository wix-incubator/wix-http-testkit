package com.wix.e2e.http.filters.internal

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.Uri.Path.{Empty, Segment, Slash}
import akka.http.scaladsl.model.{HttpMethod, HttpMethods}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.RequestFilter
import com.wix.e2e.http.WixHttpTestkitResources.materializer
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.filters.CanMatch

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try


trait BodyFilters {
  def whenBodyIs[T <: AnyRef: Manifest](expected: T)(implicit marshaller: Marshaller): RequestFilter =
    whenBodyMatches((t: T) => t == expected)

  def whenBodyMatches[T <: AnyRef : Manifest](matcher: CanMatch[T])(implicit marshaller: Marshaller): RequestFilter = { rq =>
    val str = Await.result(Unmarshal(rq.entity).to[String], 5.seconds)
    Try(marshaller.unmarshall[T](str))
                  .map( matcher.doMatch )
                  .getOrElse( false )
  }
}

trait HeaderFilters {

  def whenHeadersContain(header: (String, String), headers: (String, String)*): RequestFilter = { rq =>
    val requested = rq.headers.map(h => h.name() -> h.value()).toMap
    val expected = (header +: headers).toMap

    expected forall { case (k, v) => requested.get(k).contains(v) }
  }

  def whenHeadersMatch(matcher: CanMatch[Map[String, String]]): RequestFilter = { rq =>
    val requested = rq.headers.map(h => h.name() -> h.value()).toMap

    matcher.doMatch(requested)
  }
}

trait MethodFilters {
  def whenGet: RequestFilter = whenMethod(HttpMethods.GET)
  def whenPost: RequestFilter = whenMethod(HttpMethods.POST)
  def whenDelete: RequestFilter = whenMethod(HttpMethods.DELETE)
  def whenPut: RequestFilter = whenMethod(HttpMethods.PUT)
  def whenPatch: RequestFilter = whenMethod(HttpMethods.PATCH)

  private def whenMethod(method: HttpMethod): RequestFilter = { _.method == method }
}


trait PathFilters {

  def whenPathIs(path: String): RequestFilter = { rq =>
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

trait QueryParamFilters {

  def whenParamsContain(param: (String, String), params: (String, String)*): RequestFilter = { rq =>
    val requestedQueryParams = rq.uri.query().toMap
    val expectedQueryParams = (param +: params).toMap

    expectedQueryParams forall { case (k, v) => requestedQueryParams.get(k).contains(v) }
  }

  def whenParamsMatch(matcher: CanMatch[Map[String, String]]): RequestFilter = { rq =>
    val requestedQueryParams = rq.uri.query().toMap

    matcher.doMatch(requestedQueryParams)
  }
}
