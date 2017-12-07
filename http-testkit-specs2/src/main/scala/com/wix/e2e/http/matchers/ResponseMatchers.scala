package com.wix.e2e.http.matchers

import com.wix.e2e.http.matchers.internal._

trait ResponseMatchers extends ResponseStatusMatchers
                          with ResponseCookiesMatchers
                          with ResponseHeadersMatchers
                          with ResponseBodyMatchers
                          with ResponseSpecialHeadersMatchers
                          with ResponseBodyAndStatusMatchers
                          with ResponseStatusAndHeaderMatchers

object ResponseMatchers extends ResponseMatchers
