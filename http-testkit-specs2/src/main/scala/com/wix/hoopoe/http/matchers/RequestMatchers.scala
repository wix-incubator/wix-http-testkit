package com.wix.hoopoe.http.matchers

import com.wix.hoopoe.http.matchers.internal.{RequestMethodMatchers, RequestUrlMatchers}

trait RequestMatchers extends RequestMethodMatchers
                      with RequestUrlMatchers

object RequestMatchers extends RequestMatchers
