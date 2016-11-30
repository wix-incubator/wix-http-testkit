package com.wix.hoopoe.http.matchers.internal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import com.wix.hoopoe.http.HttpRequest
import com.wix.hoopoe.http.matchers.RequestMatcher
import org.specs2.matcher.Matchers._
import org.specs2.matcher.{Expectable, MatchResult, Matcher}

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

//  def haveHeaders(param: (String, String)): RequestMatcher = ???
//  def haveCookies(param: (String, String)): RequestMatcher = ???
//  def haveBody(param: (String, String)): RequestMatcher = ???
