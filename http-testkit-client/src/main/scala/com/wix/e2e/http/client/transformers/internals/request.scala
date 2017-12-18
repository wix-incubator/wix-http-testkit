package com.wix.e2e.http.client.transformers.internals

import java.io.File

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Cookie, RawHeader, `User-Agent`}
import akka.util.ByteString
import com.wix.e2e.http.api.Marshaller
import com.wix.e2e.http.client.transformers._
import com.wix.e2e.http.client.transformers.internals.RequestPartOps._
import com.wix.e2e.http.exceptions.UserAgentModificationNotSupportedException
import com.wix.e2e.http.{RequestTransformer, WixHttpTestkitResources}

import scala.xml.Node

trait HttpClientRequestUrlTransformers {
  def withParam(param: (String, String)): RequestTransformer = withParams(param)
  def withParams(params: (String, String)*): RequestTransformer = r =>
    r.copy(uri = r.uri
      .withQuery( Query(r.uri.rawQueryString
        .map( Query(_).toSeq )
        .getOrElse(Nil)
        ++ params: _*)) )
}

trait HttpClientRequestHeadersTransformers {
  def withHeader(header: (String, String)): RequestTransformer = withHeaders(header)
  def withHeaders(headers: (String, String)*): RequestTransformer =
    appendHeaders( headers.map {
      case (h, _) if h.toLowerCase == "user-agent" => throw new UserAgentModificationNotSupportedException
      case (h, v) => RawHeader(h, v)
    } )

  def withUserAgent(value: String): RequestTransformer = appendHeaders(Seq(`User-Agent`(value)))

  def withCookie(cookie: (String, String)): RequestTransformer = withCookies(cookie)
  def withCookies(cookies: (String, String)*): RequestTransformer = appendHeaders( cookies.map(p => Cookie(p._1, p._2)) )


  private def appendHeaders[H <: HttpHeader](headers: Iterable[H]): RequestTransformer = r =>
    r.withHeaders( r.headers ++ headers)
}

trait HttpClientRequestBodyTransformers extends HttpClientContentTypes {
  @deprecated("use `withTextPayload`", since = "Dec18, 2017")
  def withPayload(body: String, contentType: ContentType = TextPlain): RequestTransformer = withPayload(ByteString(body).toByteBuffer.array, contentType)
  def withTextPayload(body: String, contentType: ContentType = TextPlain): RequestTransformer = withPayload(ByteString(body).toByteBuffer.array, contentType)
  def withPayload(bytes: Array[Byte], contentType: ContentType): RequestTransformer = setBody(HttpEntity(contentType, bytes))
  def withPayload(xml: Node): RequestTransformer = setBody(HttpEntity(XmlContent, WixHttpTestkitResources.xmlPrinter.format(xml)))

  // todo: enable default marshaller when deprecated `withPayload` is removed
  def withPayload(entity: AnyRef)(implicit marshaller: Marshaller/* = Marshaller.Implicits.marshaller*/): RequestTransformer =
    withTextPayload(marshaller.marshall(entity), JsonContent)

  def withFormData(formParams: (String, String)*): RequestTransformer = setBody(FormData(formParams.toMap).toEntity)

  def withMultipartData(parts: (String, RequestPart)*): RequestTransformer =
    setBody( Multipart.FormData(parts.map {
      case (n, p) => Multipart.FormData.BodyPart(n, p.asBodyPartEntity, p.withAdditionalParams)
    }:_*)
      .toEntity)

  private def setBody(entity: RequestEntity): RequestTransformer = _.copy(entity = entity)
}

object RequestPartOps {

      implicit class `RequestPart --> HttpEntity`(private val r: RequestPart) extends AnyVal {
        def asBodyPartEntity: BodyPartEntity = r match {
          case PlainRequestPart(v, c) => HttpEntity(v).withContentType(c)
          case BinaryRequestPart(b, c, _) => HttpEntity(c, b)
          case FileRequestPart(f, c, _) => HttpEntity.fromPath(c, f.toPath)
          case FileNameRequestPart(p, c, fn) => FileRequestPart(new File(p), c, fn).asBodyPartEntity
        }
      }

      implicit class `RequestPart --> AdditionalParams`(private val r: RequestPart) extends AnyVal {
        def withAdditionalParams: Map[String, String] = r match {
          case _: PlainRequestPart => NoAdditionalParams
          case BinaryRequestPart(_, _, fn) => additionalParams(fn)
          case FileRequestPart(_, _, fn) => additionalParams(fn)
          case FileNameRequestPart(_, _, fn) => additionalParams(fn)
        }

        private def additionalParams(filenameOpt: Option[String]) =
          filenameOpt.map(fn => Map("filename" -> fn))
                     .getOrElse( NoAdditionalParams )

        private def NoAdditionalParams = Map.empty[String, String]
      }
}


trait HttpClientRequestTransformersOps  {
  implicit class TransformerConcatenation(first: RequestTransformer) {
    def and(second: RequestTransformer): RequestTransformer = first andThen second
  }
}
