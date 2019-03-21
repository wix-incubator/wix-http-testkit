package com.wix.e2e.http.filters


trait CanMatch[T] {
  def doMatch(t: T): Boolean
}
