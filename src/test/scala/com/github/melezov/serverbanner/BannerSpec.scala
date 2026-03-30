package com.github.melezov.serverbanner

import com.typesafe.scalalogging.StrictLogging
import org.specs2.Specification

import scala.io.Source

trait BannerSpec extends Specification with StrictLogging:

  // ### Utils ###

  def time[T](section: String)(runSection: => T): T =
    logger.trace(s"$section started ...")
    val startAt = System.currentTimeMillis
    val res = runSection
    val endAt = System.currentTimeMillis
    logger.debug(s"$section took ${endAt - startAt}ms ###")
    res

  def format(number: Int): String =
    java.text.NumberFormat
      .getInstance(java.util.Locale.ROOT)
      .format(number.toLong)

  // ### Aliases ###

  val Random: scala.util.Random.type = scala.util.Random
  type Random = scala.util.Random

  val Result: org.specs2.execute.Result.type = org.specs2.execute.Result

  // ### Resources ###

  private def sourceFrom(resource: String) =
    Source.fromInputStream(getClass.getResourceAsStream(resource))

  protected def getResourceAsString(resource: String): String =
    new String(sourceFrom(resource).toArray)

  protected def getResourceAsLines(resource: String): Iterator[String] =
    sourceFrom(resource).getLines
