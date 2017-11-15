package com.wix.e2e.http.client.drivers

import akka.http.scaladsl.model.Uri
import com.wix.e2e.http.BaseUri
import com.wix.test.random.{randomInt, randomStr}
import org.specs2.matcher.Matcher
import org.specs2.matcher.Matchers._


trait PathBuilderTestSupport {
  val contextRoot = s"/$randomStr"
  val contextRootWithMultiplePaths = s"/$randomStr/$randomStr/$randomStr"
  val relativePath = s"/$randomStr"
  val relativePathWithMultipleParts = s"/$randomStr/$randomStr/$randomStr"
  val baseUri = BaseUriGen.random
  val escapedCharacters = "!'();:@+$,/?%#[]\"'/\\" //&=
}

object BaseUriGen {
  def random: BaseUri = BaseUri(randomStr.toLowerCase, randomInt(1, 65536), Some(s"/$randomStr"))
}

object UrlBuilderMatchers {
  def beUrl(url: String): Matcher[Uri] = be_===(url) ^^ { (_: Uri).toString }
}
