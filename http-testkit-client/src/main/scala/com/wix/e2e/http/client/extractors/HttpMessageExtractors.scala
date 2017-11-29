package com.wix.e2e.http.client.extractors

import com.wix.e2e.http.client.extractors.internals.{HttpEntityTransformers, HttpRequestExtractors, HttpResponseExtractors}

trait HttpMessageExtractors extends HttpEntityTransformers
                               with HttpResponseExtractors
                               with HttpRequestExtractors

object HttpMessageExtractors extends HttpMessageExtractors