package com.wix.hoopoe.http.matchers

import com.wix.hoopoe.http.matchers.internal._

trait ResponseMatchers extends ResponseStatusMatchers
                       with ResponseCookiesMatchers
                       with ResponseHeadersMatchers

object ResponseMatchers extends ResponseMatchers
