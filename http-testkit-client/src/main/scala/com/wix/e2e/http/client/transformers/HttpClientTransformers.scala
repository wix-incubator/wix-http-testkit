package com.wix.e2e.http.client.transformers

import java.io.File

import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpCharsets, MediaTypes}
import com.wix.e2e.http.client.transformers.HttpClientContentTypes._
import com.wix.e2e.http.client.transformers.internals._

trait HttpClientTransformers extends HttpClientRequestUrlTransformers
                                with HttpClientRequestHeadersTransformers
                                with HttpClientRequestBodyTransformers
                                with HttpClientRequestTransformersOps
                                with HttpClientResponseTransformers {
}

object HttpClientTransformers extends HttpClientTransformers

trait HttpClientContentTypes {
  val TextPlain = ContentTypes.`text/plain(UTF-8)`
  val JsonContent = ContentTypes.`application/json`
  val XmlContent = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`)
  val BinaryStream = ContentType(MediaTypes.`application/octet-stream`)
  val FormUrlEncoded = ContentType(MediaTypes.`application/x-www-form-urlencoded`, HttpCharsets.`UTF-8`)
}

object HttpClientContentTypes extends HttpClientContentTypes

sealed trait RequestPart
case class PlainRequestPart(body: String, contentType: ContentType = TextPlain) extends RequestPart
case class BinaryRequestPart(body: Array[Byte], contentType: ContentType = BinaryStream) extends RequestPart
case class FileRequestPart(file: File, contentType: ContentType = BinaryStream, filename: Option[String] = None) extends RequestPart
case class BinaryAsFileRequestPart(body: Array[Byte],
                                   name: String,
                                   contentType: ContentType = BinaryStream,
                                   filename: Option[String] = None) extends RequestPart
