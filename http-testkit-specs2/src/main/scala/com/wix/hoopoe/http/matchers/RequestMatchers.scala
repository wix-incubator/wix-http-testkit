package com.wix.hoopoe.http.matchers

import com.wix.hoopoe.http.matchers.internal._

trait RequestMatchers extends RequestMethodMatchers
                      with RequestUrlMatchers
                      with RequestHeadersMatchers
                      with RequestCookiesMatchers

object RequestMatchers extends RequestMatchers
