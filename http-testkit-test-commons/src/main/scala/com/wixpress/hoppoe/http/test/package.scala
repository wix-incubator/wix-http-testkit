package com.wixpress.hoppoe.http

import scala.util.Random

package object test {

  def randomStr: String = randomStrWith(length = 20)
  def randomStrWith(length: Int): String =
    Random.alphanumeric
          .take(length).mkString


}
