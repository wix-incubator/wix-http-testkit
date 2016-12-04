package com.wix.e2e.http.api

import com.wix.e2e.http.json.JsonJacksonMarshaller

trait Marshaller {
  def unmarshall[T : Manifest](jsonStr: String): T
  def marshall[T](t: T): String
}


object Marshaller {
  implicit val marshaller: Marshaller = new JsonJacksonMarshaller
}
