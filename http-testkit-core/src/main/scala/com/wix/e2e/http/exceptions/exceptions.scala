package com.wix.e2e.http.exceptions

import com.wix.e2e.http.BaseUri

class ConnectionRefusedException(baseUri: BaseUri) extends RuntimeException(s"Unable to connect to port ${baseUri.port}")

class MissingMarshallerException extends RuntimeException(s"Unable to locate marshaller in classpath, Wix HTTP Testkit supports a default marshaller or a custom marshaller\nfor more information please check documentation at https://github.com/wix/wix-http-testkit/blob/master/MARSHALLER.md")

class MarshallerErrorException(content: String, t: Throwable) extends RuntimeException(s"Failed to unmarshall: [$content]", t)

class UserAgentModificationNotSupportedException
  extends IllegalArgumentException("`user-agent` is a special header and cannot be used in `withHeaders`. Use `withUserAgent` method instead.")

class MisconfiguredMockServerException extends RuntimeException("Mock server must have at least one handler defined\nfor more information please check documentation at https://github.com/wix/wix-http-testkit/blob/master/WEBSERVER.md")
