package com.wix.e2e.http.matchers

import com.wix.e2e.http.matchers.internal._

trait RequestMatchers extends RequestMethodMatchers
                      with RequestUrlMatchers
                      with RequestBodyMatchers
                      with RequestHeadersMatchers
                      with RequestCookiesMatchers
                      with RequestRecorderMatchers
                      with RequestContentTypeMatchers

object RequestMatchers extends RequestMatchers
