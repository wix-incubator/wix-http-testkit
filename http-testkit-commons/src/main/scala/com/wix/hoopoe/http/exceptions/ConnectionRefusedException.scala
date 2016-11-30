package com.wix.hoopoe.http.exceptions

import com.wix.hoopoe.http.BaseUri

class ConnectionRefusedException(baseUri: BaseUri) extends RuntimeException(s"Unable to connect to port ${baseUri.port}")