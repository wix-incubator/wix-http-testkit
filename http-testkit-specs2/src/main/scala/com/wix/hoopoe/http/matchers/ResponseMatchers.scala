package com.wix.hoopoe.http.matchers

import com.wix.hoopoe.http.matchers.internal.{ResponseCookiesMatchers, ResponseStatusMatchers}

trait ResponseMatchers extends ResponseStatusMatchers
                       with ResponseCookiesMatchers

object ResponseMatchers extends ResponseMatchers
