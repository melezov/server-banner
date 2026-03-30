package com.github.melezov.serverbanner

class ScrollSpec extends BannerSuite:

  test("0 x 0 fail"):
    intercept[IllegalArgumentException](Scroll(0, 1))
    intercept[IllegalArgumentException](Scroll(1, 0))

  for (w, h) <- Seq((1, 1), (2, 1), (5, 2), (12, 3), (17, 4), (27, 5), (113, 9)) do
    test(s"${w}x${h} scroll"):
      assertEquals(Scroll(w, h), getResourceAsString(s"scroll/${w}x${h}.txt"))

  test("huge scroll (100M body)"):
    val reference = getResourceAsString("scroll/113x9.txt")
    val resizableHeight = reference.count(_ == '\n') - 3
    val bodyWidth = 7 * 1000 * 1000

    val render = time(s"Creating ~ ${format(bodyWidth * resizableHeight)}-char wide scroll"):
      Scroll(bodyWidth, 9)

    val lengthDifference = bodyWidth - 113
    assertEquals(render.length, reference.length + lengthDifference * resizableHeight)
