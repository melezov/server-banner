package com.github.melezov.serverbanner

import Main.{Action, ColorMode, Config}

class BannerCliSpec extends BannerSuite:

  // ### Argument parsing ###

  test("no arguments returns error"):
    assert(Main.parseArgs(Array.empty).isLeft)

  test("banner text only"):
    assertEquals(Main.parseArgs(Array("HT-California-02")), Right(Action.Run(Config("HT-California-02", None, ColorMode.Auto))))

  test("banner text with --greeting"):
    assertEquals(
      Main.parseArgs(Array("--greeting", "Plenty of room at the", "HT-California-02")),
      Right(Action.Run(Config("HT-California-02", Some("Plenty of room at the"), ColorMode.Auto))),
    )

  test("banner text with -g shorthand"):
    assertEquals(
      Main.parseArgs(Array("-g", "Such a lovely place", "HT-California-02")),
      Right(Action.Run(Config("HT-California-02", Some("Such a lovely place"), ColorMode.Auto))),
    )

  test("greeting after banner text"):
    assertEquals(
      Main.parseArgs(Array("HT-California-02", "--greeting", "You can check out any time you like")),
      Right(Action.Run(Config("HT-California-02", Some("You can check out any time you like"), ColorMode.Auto))),
    )

  // ### --color flag ###

  test("--color off disables color"):
    assertEquals(
      Main.parseArgs(Array("--color", "off", "HT-California-02")),
      Right(Action.Run(Config("HT-California-02", None, ColorMode.Off))),
    )

  test("--color on enables color"):
    assertEquals(
      Main.parseArgs(Array("--color", "on", "HT-California-02")),
      Right(Action.Run(Config("HT-California-02", None, ColorMode.On))),
    )

  test("--color auto is the default"):
    assertEquals(
      Main.parseArgs(Array("--color", "auto", "HT-California-02")),
      Right(Action.Run(Config("HT-California-02", None, ColorMode.Auto))),
    )

  test("default color mode is Auto"):
    val Right(Action.Run(config)) = Main.parseArgs(Array("HT-California-02")): @unchecked
    assertEquals(config.colorMode, ColorMode.Auto)

  test("invalid --color value"):
    assert(Main.parseArgs(Array("--color", "maybe", "HT-California-02")).isLeft)

  test("--color without value"):
    assert(Main.parseArgs(Array("--color")).isLeft)

  test("--help with --color off"):
    assertEquals(
      Main.parseArgs(Array("--color", "off", "--help")),
      Right(Action.Help(ColorMode.Off)),
    )

  test("--help returns Help with Auto color"):
    assertEquals(Main.parseArgs(Array("--help")), Right(Action.Help(ColorMode.Auto)))

  // ### resolveColor ###

  test("resolveColor On always returns true"):
    assertEquals(Main.resolveColor(ColorMode.On), true)

  test("resolveColor Off always returns false"):
    assertEquals(Main.resolveColor(ColorMode.Off), false)

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

  test("color off has no escape codes"):
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

  // ### Help output ###

  test("plain help has no escape codes"):
    val output = Main.help(false)
    assert(!output.contains("\u001b"), "plain help should not contain escape codes")
    assert(output.contains("--color <auto|on|off>"), "should document --color flag")
    assert(output.contains(EmbeddedResources.version), "should contain version")
    assert(output.contains("https://github.com/github/melezov/server-banner"), "should contain project URL")

  test("colored help has ANSI escape codes"):
    val output = Main.help(true)
    assert(output.contains("\u001b["), "colored help should contain ANSI escape codes")
    assert(output.contains("--color <auto|on|off>"), "should document --color flag")
    assert(output.contains(Color.Yellow.ansiCode), "should contain yellow for headers")
    assert(output.contains(Color.Green.ansiCode), "should contain green for options")
    assert(output.contains(EmbeddedResources.version), "should contain version")
