package com.wix.e2e.http.matchers.drivers

import com.wix.e2e.http.HttpRequest
import com.wix.e2e.http.api.RequestRecordSupport
import com.wix.e2e.http.matchers.drivers.HttpRequestFactory.aRandomRequest
import com.wix.e2e.http.matchers.drivers.RequestRecorderFactory._

trait RequestRecordTestSupport {
  val request = aRandomRequest
  val anotherRequest = aRandomRequest
  val yetAnotherRequest = aRandomRequest
  val andAnotherRequest = aRandomRequest

  val anEmptyRequestRecorder = aRequestRecorderWith()

}

object RequestRecorderFactory {

  def aRequestRecorderWith(requests: HttpRequest*) = new RequestRecordSupport {
    val recordedRequests = requests
    def clearRecordedRequests() = {}
  }
}
