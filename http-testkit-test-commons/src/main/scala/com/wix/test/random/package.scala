package com.wix.test

import scala.util.Random

package object random {

  def randomStr: String = randomStrWith(length = 20)
  def randomStrWith(length: Int): String =
    Random.alphanumeric
          .take(length).mkString
  def randomStrPair = randomStr -> randomStr

  def randomInt: Int = Random.nextInt()

  def randomBytes(length:Int): Array[Byte] = {
    val result = Array.ofDim[Byte](length)
    Random.nextBytes(result)
    result
  }

  def randomInt(from: Int, to: Int): Int = {
    require(math.abs(to.toDouble - from.toDouble) <= Int.MaxValue.toDouble, s"Range can't exceed ${Int.MaxValue}")
    from + Random.nextInt(math.max(to - from, 1))
  }


  def randomPort = randomInt(0, 65535)
}
