package com.wix.e2e.http.drivers

import java.nio.file.{Files, Path}

import akka.http.scaladsl.model.HttpRequest
import com.wix.e2e.http.HttpRequest
import com.wix.e2e.http.client.extractors._
import com.wix.e2e.http.client.transformers._
import com.wix.e2e.http.matchers.RequestMatcher
import com.wix.test.random._
import org.specs2.matcher.Matchers.contain

trait HttpClientTransformersTestSupport {
  val request = HttpRequest()

  val keyValue1 = randomStrPair
  val keyValue2 = randomStrPair
  val keyValue3 = randomStrPair
  val escapedCharacters = "!'();:@&=+$,/?%#[]\"'/\\"
  val userAgent = randomStr
  val someBody = randomStr
  val someBytes = randomBytes(100)
  val payload = SomePayload(randomStr, randomStr)
  val strBody = randomStr

  val partName = randomStr
  val fileNameOpt = randomStrOpt

  val plainRequestPart = randomStr -> PlainRequestPart(randomStr)
  val plainRequestXmlPart = randomStr -> PlainRequestPart(randomStr, HttpClientContentTypes.XmlContent)
  val binaryRequestPart = randomStr -> BinaryRequestPart(randomBytes(20))
  val binaryRequestXmlPart = randomStr -> BinaryRequestPart(randomBytes(20), HttpClientContentTypes.XmlContent)
  val binaryRequestXmlPartAndFilename = randomStr -> BinaryRequestPart(randomBytes(20), HttpClientContentTypes.XmlContent, fileNameOpt)


  def givenFileWith(content: Array[Byte]): Path = {
    val f = Files.createTempFile("multipart", ".tmp")
    Files.write(f, content)
    f
  }
}

case class SomePayload(key: String, value: String)

object HttpClientTransformersMatchers extends HttpClientTransformers {

  def haveBodyPartWith(part: (String, PlainRequestPart)): RequestMatcher =
    ( contain(s"""Content-Disposition: form-data; name="${part._1}"""") and
      contain(s"""Content-Type: ${part._2.contentType.value}""") and
      contain(part._2.body) ) ^^ { (_: HttpRequest).entity.extractAsString }

  // todo: matcher binary data on multipart request
  def haveBinaryBodyPartWith(part: (String, BinaryRequestPart)): RequestMatcher =
    ( contain(s"""Content-Disposition: form-data;""") and
      contain(s"""; name="${part._1}"""") and
      (if (part._2.filename.isEmpty) contain(";") else contain(s"""; filename="${part._2.filename.get}""")) and
      contain(s"""Content-Type: ${part._2.contentType.value}""") and
      contain(s"""Content-Type: ${part._2.contentType.value}""") /*and
      contain(part._2.body)*/ ) ^^ { (_: HttpRequest).entity.extractAsString } // todo: match body

  def haveFileBodyPartWith(part: (String, FileRequestPart)): RequestMatcher =
    ( contain(s"""Content-Disposition: form-data;""") and
      contain(s"""; name="${part._1}"""") and
      (if (part._2.filename.isEmpty) contain(";") else contain(s"""; filename="${part._2.filename.get}""")) and
      contain(s"""Content-Type: ${part._2.contentType.value}""") and
      contain(s"""Content-Type: ${part._2.contentType.value}""") /*and
      contain(part._2.body)*/ ) ^^ { (_: HttpRequest).entity.extractAsString } // todo: match body

}
