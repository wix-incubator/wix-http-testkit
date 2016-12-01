package com.wix.e2e.http.matchers.internal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{Cookie, HttpCookiePair}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.wix.e2e.http.HttpRequest
import com.wix.e2e.http.json.{DefaultMarshaller, Marshaller}
import com.wix.e2e.http.matchers.RequestMatcher
import com.wix.e2e.http.utils._
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

import scala.concurrent.ExecutionContext
import scala.util.Try

trait RequestMethodMatchers {
  def bePost: RequestMatcher = beRequestWith( POST )
  def beGet: RequestMatcher = beRequestWith( GET )
  def beDelete: RequestMatcher = beRequestWith( DELETE )
  def beHead: RequestMatcher = beRequestWith( HEAD )
  def beOptions: RequestMatcher = beRequestWith( OPTIONS )
  def bePatch: RequestMatcher = beRequestWith( PATCH )
  def bePut: RequestMatcher = beRequestWith( PUT )
  def beTrace: RequestMatcher = beRequestWith( TRACE )
  def beConnect: RequestMatcher = beRequestWith( CONNECT )

  private def beRequestWith(method: HttpMethod): RequestMatcher =
    be_===( method ) ^^ { (_: HttpRequest).method aka "request method" }
}

trait RequestUrlMatchers {
  def havePath(path: String): RequestMatcher = havePathThat(must = be_===( path ) )
  def havePathThat(must: Matcher[String]): RequestMatcher = must ^^ { (_: HttpRequest).uri.path.toString aka "request path"}

  def haveAnyParamOf(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, _.identical.nonEmpty,
                          req => s"Could not find parameter [${req.missing.map(_._1).mkString(", ")}] but found those: [${req.extra.map(_._1).mkString(", ")}]")

  def haveAllParamFrom(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, _.missing.isEmpty,
                          req => s"Could not find parameter [${req.missing.map(_._1).mkString(", ")}] but found those: [${req.identical.map(_._1).mkString(", ")}]." )

  def haveTheSameParamsAs(params: (String, String)*): RequestMatcher =
    haveParameterInternal(params, r => r.extra.isEmpty && r.missing.isEmpty,
                          req => s"Request parameters are not identical, missing parameters from request: [${req.missing.map(_._1).mkString(", ")}], request contained extra parameters: [${req.extra.map(_._1).mkString(", ")}]." )

  private def haveParameterInternal(params: Seq[(String, String)], comparator: ParamterComparisonResult => Boolean, errorMessage: ParamterComparisonResult => String): RequestMatcher = new RequestMatcher {

    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val requestParameters = request.uri.query()
      val comparisonResult = compare(params, requestParameters)

      if ( comparator(comparisonResult) ) success("ok", t)
      else if (requestParameters.isEmpty) failure("Request did not contain any request parameters.", t)
      else failure(errorMessage(comparisonResult), t)
    }

    private def compareParam(param1: (String, String), param2: (String, String)) = param1._1 == param2._1 && param1._2 == param2._2

    private def compare(params: Seq[(String, String)], requestParams: Seq[(String, String)]): ParamterComparisonResult = {
      val identical = params.filter( h1 => requestParams.exists( h2 => compareParam(h1, h2) ) )
      val missing = params.filter( h1 => !identical.exists( h2 => compareParam(h1, h2) ) )
      val extra = requestParams.filter( h1 => !identical.exists( h2 => compareParam(h1, h2) ) )

      ParamterComparisonResult(identical, missing, extra)
    }
  }

  def haveAnyParamThat(must: Matcher[String], withParamName: String): RequestMatcher = new RequestMatcher {
    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val requestParameters = request.uri.query()
      val requestParameter = requestParameters.find( _._1 == withParamName )
                                              .map( _._2 )

      requestParameter match {
        case None if requestParameters.isEmpty => failure("Request did not contain any parameters.", t)
        case None => failure(s"Request contain parameter names: [${requestParameters.map( _._1 ).mkString(", ")}] which did not contain: [$withParamName]", t)
        case Some(value) if must.apply(createExpectable(value)).isSuccess => success("ok", t)
        case Some(value) => failure(s"Request parameter [$withParamName], did not match { ${must.apply(createExpectable(value)).message} }", t)
      }

    }
  }

  private case class ParamterComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])
}

trait RequestHeadersMatchers {

  def haveAnyOf(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, _.identical.nonEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.extra.map(_._1).mkString(", ")}]" )

  def haveAllOf(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, _.missing.isEmpty,
      res => s"Could not find header [${res.missing.map(_._1).mkString(", ")}] but found those: [${res.identical.map(_._1).mkString(", ")}]." )

  def haveTheSameHeadersAs(headers: (String, String)*): RequestMatcher =
    haveHeaderInternal( headers, r => r.extra.isEmpty && r.missing.isEmpty,
      res => s"Request header is not identical, missing headers from request: [${res.missing.map(_._1).mkString(", ")}], request contained extra headers: [${res.extra.map(_._1).mkString(", ")}]." )

  private def haveHeaderInternal(headers: Seq[(String, String)], comparator: HeaderComparisonResult => Boolean, errorMessage: HeaderComparisonResult => String): RequestMatcher = new RequestMatcher {

    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val requestHeaders = request.headers
                                  .filterNot( _.isInstanceOf[Cookie] )
                                  .map( h => h.name -> h.value )
      val comparisonResult = compare(headers, requestHeaders)

      if ( comparator(comparisonResult) ) success("ok", t)
      else if (requestHeaders.isEmpty) failure("Request did not contain any headers.", t)
      else failure(errorMessage(comparisonResult), t)
    }

    private def compareHeader(header1: (String, String), header2: (String, String)) = header1._1.toLowerCase == header2._1.toLowerCase && header1._2 == header2._2

    private def compare(headers: Seq[(String, String)], requestHeaders: Seq[(String, String)]): HeaderComparisonResult = {
      val identical = headers.filter( h1 => requestHeaders.exists( h2 => compareHeader(h1, h2) ) )
      val missing = headers.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )
      val extra = requestHeaders.filter( h1 => !identical.exists( h2 => compareHeader(h1, h2) ) )

      HeaderComparisonResult(identical, missing, extra)
    }
  }

  def haveAnyHeaderThat(must: Matcher[String], withHeaderName: String): RequestMatcher = new RequestMatcher {

    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val headers = request.headers
                           .filterNot( _.isInstanceOf[Cookie] )
      val requestHeader = headers.find( _.name.toLowerCase == withHeaderName.toLowerCase )
                                 .map( _.value )

      requestHeader match {
        case None if headers.isEmpty => failure("Request did not contain any headers.", t)
        case None => failure(s"Request contain header names: [${headers.map( _.name ).mkString(", ")}] which did not contain: [$withHeaderName]", t)
        case Some(value) if must.apply(createExpectable(value)).isSuccess => success("ok", t)
        case Some(value) => failure(s"Request header [$withHeaderName], did not match { ${must.apply(createExpectable(value)).message} }", t)
      }
    }
  }

  private case class HeaderComparisonResult(identical: Seq[(String, String)], missing: Seq[(String, String)], extra: Seq[(String, String)])

}

trait RequestCookiesMatchers {
  def receivedCookieWith(name: String): RequestMatcher = receivedCookieThat(must = be_===(name) ^^ { (_: HttpCookiePair).name aka "cookie name" })

  def receivedCookieThat(must: Matcher[HttpCookiePair]): RequestMatcher = new RequestMatcher {
    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val cookies = request.headers
                           .collect { case Cookie(cookie) => cookie }
                           .flatten[HttpCookiePair]
      val matchResult = cookies.map( c => must.apply(createExpectable(c)) )
      if (matchResult.exists( _.isSuccess) ) success("ok", t)
      else if (matchResult.isEmpty) failure("Request did not contain any Cookie headers.", t)
      else failure(s"Could not find cookie that [${matchResult.map( _.message ).mkString(", ")}].", t)
    }
  }
}

trait RequestBodyMatchers {
  import com.wix.e2e.http.WixHttpTestkitResources.materializer

  import ExecutionContext.Implicits.global


  def haveBodyWith(bodyContent: String): RequestMatcher = haveBodyThat( must = be_===(bodyContent) )
  def haveBodyThat(must: Matcher[String]): RequestMatcher = must ^^ httpRequestAsString

  def haveBodyWith(data: Array[Byte]): RequestMatcher = haveBodyDataThat( must = be_===(data) )
  def haveBodyDataThat(must: Matcher[Array[Byte]]): RequestMatcher = must ^^ httpRequestAsBinary

  def havePayloadWith[T <: AnyRef : Manifest](entity: T)(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): RequestMatcher = havePayloadThat[T]( must = be_===(entity) )
  def havePayloadThat[T <: AnyRef : Manifest](must: Matcher[T])(implicit marshaller: Marshaller = DefaultMarshaller.marshaller): RequestMatcher = new RequestMatcher {

    def apply[S <: HttpRequest](t: Expectable[S]): MatchResult[S] = {
      val request = t.value
      val content = waitFor( Unmarshal(request.entity).to[String] )

      Try( marshaller.unmarshall[T](content) ).toOption match {
        case None => failure(s"Failed to unmarshall: [$content]", t)
        case Some(x) if must.apply(createExpectable(x)).isSuccess => success("ok", t)
        case Some(x) => failure(s"Failed to match: [${must.apply(createExpectable(x)).message.replaceAll("\n", "")}] with content: [$content]", t)
      }
    }
  }

  private def httpRequestAsString = (r: HttpRequest) => waitFor( Unmarshal(r.entity).to[String] ) aka "Body content as string"
  private def httpRequestAsBinary = (r: HttpRequest) => waitFor( Unmarshal(r.entity).to[Array[Byte]] ) aka "Body content as bytes"
}
