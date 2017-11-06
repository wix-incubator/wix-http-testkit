package com.wix.e2e.http.client.transformers

import com.wix.e2e.http.client.transformers.internals._

trait HttpClientTransformers extends HttpClientRequestUrlTransformers
                                with HttpClientRequestHeadersTransformers
                                with HttpClientRequestBodyTransformers
                                with HttpClientRequestTransformersOps
                                with HttpClientResponseTransformers

object HttpClientTransformers extends HttpClientTransformers