package com.wix.e2e.http.matchers

import com.wix.e2e.http.matchers.internal._

trait RequestMatchers extends RequestMethodMatchers
                      with RequestUrlMatchers
                      with RequestHeadersMatchers
                      with RequestCookiesMatchers
                      with RequestBodyMatchers

object RequestMatchers extends RequestMatchers
