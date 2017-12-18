package com.wix.e2e.http.client

import java.net.URLEncoder

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpCookiePair
import com.wix.e2e.http.api.Marshaller.Implicits._
import com.wix.e2e.http.client.transformers._
import com.wix.e2e.http.drivers.HttpClientTransformersMatchers._
import com.wix.e2e.http.drivers.HttpClientTransformersTestSupport
import com.wix.e2e.http.exceptions.UserAgentModificationNotSupportedException
import com.wix.e2e.http.matchers.RequestMatchers._
import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class HttpClientTransformersTest extends Spec with HttpClientTransformers {

  trait ctx extends Scope with HttpClientTransformersTestSupport


  "Parameter Request Transformers" should {

    "add parameter to requested URI" in new ctx {
      withParam(keyValue1)(request) must haveTheSameParamsAs(keyValue1)
    }

    "add multi-value parameters to requested URI" in new ctx {
      withParams(keyValue1, keyValue2)(request) must haveTheSameParamsAs(keyValue1, keyValue2)
    }

    "chain several add parameters calls together" in new ctx {
      (withParams(keyValue1) and
        withParam(keyValue2)) (request) must
        haveTheSameParamsAs(keyValue1, keyValue2)
    }

  }

  "Header Request Transformers" should {

    "be able to add header to request" in new ctx {
      withHeaders(keyValue1, keyValue2)(request) must haveTheSameHeadersAs(keyValue1, keyValue2)
    }

    "chain more than one add headers calls to request" in new ctx {
      (withHeader(keyValue1) and
        withHeader(keyValue2)) (request) must haveTheSameHeadersAs(keyValue1, keyValue2)
    }

    "allow modifying user agent of the client" in new ctx {
      withUserAgent(userAgent)(request) must haveTheSameHeadersAs("user-agent" -> userAgent)
    }

    "throw an exception if trying to modify user-agent with with header transformer" in new ctx {
      withHeader("user-agent" -> userAgent)(request) must throwAn[UserAgentModificationNotSupportedException]
      withHeaders("user-agent" -> userAgent)(request) must throwAn[UserAgentModificationNotSupportedException]
    }

  }

  "Cookie Request Transformers" should {

    "add cookie to request" in new ctx {
      withCookie(keyValue1)(request) must receivedCookieThat(be_===(HttpCookiePair(keyValue1)))
    }

    "add cookies to request" in new ctx {
      withCookies(keyValue1, keyValue2)(request) must { receivedCookieThat(be_===(HttpCookiePair(keyValue1))) and
                                                        receivedCookieThat(be_===(HttpCookiePair(keyValue2))) }
    }

    "chain add cookies to request" in new ctx {
      (withCookies(keyValue1) and
       withCookie(keyValue2))(request) must { receivedCookieThat(be_===(HttpCookiePair(keyValue1))) and
                                              receivedCookieThat(be_===(HttpCookiePair(keyValue2))) }
    }
  }

  "Body Request Transformers" should {

    "add string payload with content type" in new ctx {
      withTextPayload(body = someBody)(request) must (haveTextPlainBody and haveBodyWith(someBody))

      withTextPayload(someBody, contentType = TextPlain)(request) must (haveTextPlainBody and haveBodyWith(someBody))
      withTextPayload(someBody, contentType = JsonContent)(request) must (haveJsonBody and haveBodyWith(someBody))
    }

    "add byte array payload with custom content type" in new ctx {
      val customType = ContentTypes.`application/octet-stream`
      withPayload(someBytes, contentType = customType)(request) must haveBodyWith(someBytes)
    }

    "added objects will be converted to json" in new ctx {
      withPayload(payload).apply(request) must haveBodyWith(payload)
    }

    "add XML as payload" in new ctx {
      withPayload(<element attribute="value"/>)(request) must haveBodyWith("""<element attribute="value"/>""")
    }

    "add form url encoded payload" in new ctx {
      withFormData(keyValue1, "escaped-characters" -> escapedCharacters)(request) must
        ( haveFormUrlEncodedBody and haveBodyWith(bodyContent = s"${keyValue1._1}=${keyValue1._2}&escaped-characters=${URLEncoder.encode(escapedCharacters, "UTF-8")}") )
    }
  }

  "Multipart Body Request Transformers" should {

    "add multipart request data with simple string type" in new ctx {
      withMultipartData(plainRequestPart)(request) must
        ( haveMultipartFormBody and haveBodyPartWith(plainRequestPart) )
    }

    "add multipart request data with simple string type and content type" in new ctx {
      withMultipartData(plainRequestXmlPart)(request) must
        ( haveMultipartFormBody and haveBodyPartWith(plainRequestXmlPart) )
    }

    "add multipart request data with binary type" in new ctx {
      withMultipartData(binaryRequestXmlPart)(request) must
        ( haveMultipartFormBody and haveBinaryBodyPartWith(binaryRequestXmlPart) )
    }

    "add multipart request data with binary type and filename" in new ctx {
      withMultipartData(binaryRequestXmlPartAndFilename)(request) must
        ( haveMultipartFormBody and haveBinaryBodyPartWith(binaryRequestXmlPartAndFilename) )
    }

    "add multipart request data with binary type and content type" in new ctx {
      withMultipartData(binaryRequestPart)(request) must
        ( haveMultipartFormBody and haveBinaryBodyPartWith(binaryRequestPart) )
    }

    "add multipart file data with binary type" in new ctx {
      val f = givenFileWith(someBytes)
      val fileRequestPart = partName -> FileRequestPart(f.toFile)

      withMultipartData(partName -> FileRequestPart(f.toFile))(request) must
        ( haveMultipartFormBody and haveFileBodyPartWith(partName -> FileRequestPart(f.toFile)) )
    }

    "add multipart file data with binary type and content type" in new ctx {
      val f = givenFileWith(someBytes)
      val fileRequestPart = partName -> FileRequestPart(f.toFile, HttpClientContentTypes.XmlContent)

      withMultipartData(fileRequestPart)(request) must
        ( haveMultipartFormBody and haveFileBodyPartWith(fileRequestPart) )
    }

    "add multipart file with custom filename" in new ctx {
      val f = givenFileWith(someBytes)
      val fileRequestPart = partName -> FileRequestPart(f.toFile, filename = fileNameOpt)

      withMultipartData(fileRequestPart)(request) must
        ( haveMultipartFormBody and haveFileBodyPartWith(fileRequestPart) )
    }
  }

  "Chained Transformers" should {

    "have all handlers chained together without loosing data" in new ctx {
      (withParam(keyValue1) and
       withHeader(keyValue2) and
       withCookie(keyValue3) and
       withPayload(payload))(request) must
        { haveTheSameParamsAs(keyValue1) and
          haveAllHeadersOf(keyValue2) and
          receivedCookieThat(be_===(HttpCookiePair(keyValue3))) and
          haveBodyWith(payload) }
    }

    "have all handlers chained together without loosing data with form data" in new ctx {
      (withParam(keyValue1) and
       withHeader(keyValue2) and
       withCookie(keyValue3) and
       withFormData(keyValue1))(request) must
        { haveTheSameParamsAs(keyValue1) and
          haveAllHeadersOf(keyValue2) and
          receivedCookieThat(be_===(HttpCookiePair(keyValue3))) and
          haveFormUrlEncodedBody and
          haveBodyWith(bodyContent = s"${keyValue1._1}=${keyValue1._2}") }
    }
  }
}


