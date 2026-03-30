package com.github.melezov.serverbanner

import Slant.Glyph
import scala.concurrent.duration.*

class SlantSpec extends BannerSuite:
  override val munitTimeout = 5.minutes

  private val SortedChars: IndexedSeq[Char] =
    Slant.AllowedChars.toIndexedSeq.sorted

  test("allowed"):
    assert(Slant(SortedChars.mkString).nonEmpty)

  test("disallowed"):
    val ex = intercept[IllegalArgumentException]:
      Slant("Dis ? is not allowed ?!?")
    assert(ex.getMessage.contains("' ', '?', '!'"), ex.getMessage)

  test("single glyph"):
    for ch <- SortedChars do
      val actual = Slant(new String(Array(ch)))
      val expected = Glyph.find(ch, None).toString
      assertEquals(actual, expected, s"glyph '$ch'")

  test("glyph pairs"):
    for ch1 <- SortedChars; ch2 <- SortedChars do
      val actual = Slant(new String(Array(ch1, ch2)))
      val expected = Glyph.find(ch2, Some(ch1)).toString
      assertEquals(actual, expected, s"pair '$ch1$ch2'")

  test("use cases"):
    val ucs = getResourceAsLines("slant/use-cases.txt")
      .grouped(1 + Glyph.Height)
      .toSeq

    for uc <- ucs do
      val actual = Slant(uc.head)
      val expected = uc.tail.mkString("", "\n", "\n")
      assertEquals(actual, expected, s"use case '${uc.head}'")

  test("3-combinations and variations"):
    val lines = getResourceAsLines("slant/3-combinations.txt")
      .grouped(Glyph.Height)

    val combs = Combinations(SortedChars.mkString, 3)
    var passed = 0
    for chars <- combs do
      val actual = Slant(chars)
      val expected = lines.next().mkString("", "\n", "\n")
      assertEquals(actual, expected, s"combination '$chars'")
      passed += 1
    assertEquals(passed, combs.length)

  test("speed test (100K chars)"):
    val charsLength = 100 * 1000
    val rnd = scala.util.Random()
    val text =
      val sb = StringBuilder()
      (1 to charsLength).foreach: _ =>
        sb += SortedChars(rnd.nextInt(SortedChars.length))
      sb.toString

    val render = time(s"Formatting ${format(charsLength)} chars"):
      Slant(text)

    assertEquals(render.count(_ == '\n'), Glyph.Height)
    assert(render.length > Glyph.Height * charsLength)
