package com.github.melezov.serverbanner

class BannerCliSpec extends BannerSuite:

  // ### Argument parsing ###

  test("no arguments returns None"):
    assertEquals(Main.parseArgs(Array.empty), Right(None))

  test("banner text only"):
    assertEquals(Main.parseArgs(Array("HT-California-02")), Right(Some(Main.Config("HT-California-02", None, true))))

  test("banner text with --greeting"):
    assertEquals(
      Main.parseArgs(Array("--greeting", "Plenty of room at the", "HT-California-02")),
      Right(Some(Main.Config("HT-California-02", Some("Plenty of room at the"), true))),
    )

  test("banner text with -g shorthand"):
    assertEquals(
      Main.parseArgs(Array("-g", "Such a lovely place", "HT-California-02")),
      Right(Some(Main.Config("HT-California-02", Some("Such a lovely place"), true))),
    )

  test("greeting after banner text"):
    assertEquals(
      Main.parseArgs(Array("HT-California-02", "--greeting", "You can check out any time you like")),
      Right(Some(Main.Config("HT-California-02", Some("You can check out any time you like"), true))),
    )

  // ### --no-color ###

  test("--no-color disables color"):
    assertEquals(
      Main.parseArgs(Array("--no-color", "HT-California-02")),
      Right(Some(Main.Config("HT-California-02", None, false))),
    )

  test("default is color enabled"):
    val Right(Some(config)) = Main.parseArgs(Array("HT-California-02")): @unchecked
    assertEquals(config.color, true)

  // ### Error cases ###

  test("--greeting without value"):
    assert(Main.parseArgs(Array("--greeting")).isLeft)

  test("unknown option"):
    assert(Main.parseArgs(Array("--unknown", "HT-California-02")).isLeft)

  test("missing banner text"):
    assert(Main.parseArgs(Array("--greeting", "Such a lovely place")).isLeft)

  test("extra positional argument"):
    assert(Main.parseArgs(Array("HT-California-02", "extra")).isLeft)

  // ### Rendering ###

  test("render with banner text only"):
    val output = Banner.render("HT-California-02", None, true)
    assert(output.contains(".---."), "should contain scroll decoration")
    assert(output.nonEmpty)

  test("render with greeting"):
    val output = Banner.render("HT-California-02", Some("Such a lovely place"), true)
    assert(output.contains("S U C H  A  L O V E L Y  P L A C E"), "should contain spaced greeting")

  test("default banner renders"):
    val output = Banner.render(Banner.DefaultBannerText, Some(Banner.DefaultGreeting), true)
    assert(output.contains(".---."), "should contain scroll decoration")
    assert(output.contains("S C A L A"), "should contain greeting")

  // ### Color output ###

  test("--no-color has no escape codes"):
    val output = Banner.render("HT-California-02", Some("Mirrors on the ceiling"), false)
    assert(!output.contains("\u001b"), "no-color mode should not contain escape codes")

  test("color output has ANSI escape codes"):
    val output = Banner.render("HT-California-02", Some("Mirrors on the ceiling"), true)
    assert(output.contains("\u001b["), "color mode should contain ANSI escape codes")
    assert(output.contains("\u001b[31m"), "should contain red for scroll")
    assert(output.contains("\u001b[33m"), "should contain yellow for banner")
    assert(output.contains("\u001b[32m"), "should contain green for greeting")
    assert(output.endsWith(Color.AnsiReset), "should end with reset code")

  test("colored output avoids redundant escape codes"):
    val output = Banner.render("HT-California-02", None, true)
    val redCount = "\u001b\\[31m".r.findAllIn(output).length
    assert(redCount < output.count(_ == '|'), "should not emit red escape for every red character")
