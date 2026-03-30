package com.github.melezov.serverbanner

import scala.io.Source

abstract class BannerSuite extends munit.FunSuite:

  // ### Utils ###

  def time[T](section: String)(body: => T): T =
    val start = System.currentTimeMillis
    val result = body
    val elapsed = System.currentTimeMillis - start
    println(s"$section took ${elapsed}ms")
    result

  def format(number: Int): String =
    val s = number.toString
    val sb = StringBuilder()
    var i = 0
    for ch <- s do
      if i > 0 && (s.length - i) % 3 == 0 then sb += ','
      sb += ch
      i += 1
    sb.toString

  // ### Resources ###

  private def sourceFrom(resource: String) =
    Source.fromFile(s"src/test/resources/com/github/melezov/serverbanner/$resource")

  def getResourceAsString(resource: String): String =
    val source = sourceFrom(resource)
    try new String(source.toArray) finally source.close()

  def getResourceAsLines(resource: String): Iterator[String] =
    val source = sourceFrom(resource)
    try source.getLines().toList.iterator finally source.close()
