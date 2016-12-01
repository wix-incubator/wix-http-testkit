package com.wix.e2e.http.exceptions

import com.wix.e2e.http.BaseUri

class ConnectionRefusedException(baseUri: BaseUri) extends RuntimeException(s"Unable to connect to port ${baseUri.port}")