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

  // ### --version flag ###

  test("--version returns Version action"):
    assertEquals(Main.parseArgs(Array("--version")), Right(Action.Version))

  test("--version ignores other arguments"):
    assertEquals(Main.parseArgs(Array("--color", "off", "--version", "My-Server")), Right(Action.Version))

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
    assert(output.contains(Color.Red.ansiCode), "should contain red for scroll")
    assert(output.contains(Color.Yellow.ansiCode), "should contain yellow for banner")
    assert(output.contains(Color.Green.ansiCode), "should contain green for greeting")
    assert(output.endsWith(Color.AnsiReset + "\n"), "should end with reset code")

  test("colored output avoids redundant escape codes"):
    val output = Banner.render("HT-California-02", None, true)
    val redCount = "\u001b\\[31m".r.findAllIn(output).length
    assert(redCount < output.count(_ == '|'), "should not emit red escape for every red character")

  // ### Greeting overwrites scroll ###

  test("greeting overwrites scroll graphics"):
    val output = Banner.render("Hi", Some("Such a lovely place"), false)
    val lines = output.split('\n')
    // Greeting is at y=2, x=8 - it should overwrite the scroll graphics on that line
    val greetingLine = lines(2)
    val greeting = Greeting("Such a lovely place").trim
    assert(greetingLine.contains(greeting), s"greeting line should contain '$greeting' but was '$greetingLine'")

  test("greeting spaces clear scroll graphics"):
    val output = Banner.render("Hi", Some("Such a lovely place"), false)
    val lines = output.split('\n')
    val greetingLine = lines(2)
    val greeting = Greeting("Such a lovely place").trim
    val greetingStart = greetingLine.indexOf(greeting)
    assert(greetingStart >= 0, "greeting should be present in line")
    // The greeting area should be exactly the greeting text (spaces included), not mixed with scroll chars
    val greetingArea = greetingLine.substring(greetingStart, greetingStart + greeting.length)
    assertEquals(greetingArea, greeting)

  test("long greeting enlarges scroll"):
    val shortOutput = Banner.render("Hi", None, false)
    val shortWidth = shortOutput.split('\n').map(_.length).max
    val longGreeting = "This is an extremely long greeting that should enlarge the scroll beyond its normal width"
    val longOutput = Banner.render("Hi", Some(longGreeting), false)
    val longWidth = longOutput.split('\n').map(_.length).max
    val greetingWidth = Greeting(longGreeting).trim.length
    assert(longWidth > shortWidth, s"long greeting should enlarge the drawing: $longWidth > $shortWidth")
    assert(longWidth >= greetingWidth + 8, s"canvas should be wide enough for greeting at x=8: $longWidth >= ${greetingWidth + 8}")

  test("greeting overwrites scroll border characters"):
    // Use a greeting long enough to reach the scroll's right-side decorations on line 2
    val longGreeting = "A very long greeting text that goes over the scroll border decorations"
    val output = Banner.render("Hi", Some(longGreeting), false)
    val lines = output.split('\n')
    val greetingLine = lines(2)
    val greeting = Greeting(longGreeting).trim
    // The greeting should fully appear, overwriting any scroll graphics
    assert(greetingLine.contains(greeting), s"long greeting should overwrite scroll border characters")

  // ### Unsupported characters ###

  test("unsupported characters in banner text are detected"):
    val disallowed = "Hello World!".filterNot(Slant.AllowedChars).distinct
    assert(disallowed.nonEmpty, "test input should contain disallowed characters")
    assert(disallowed.contains(' '), "space should be disallowed")
    assert(disallowed.contains('!'), "! should be disallowed")

  // ### Help output ###

  test("plain help has no escape codes"):
    val output = Main.help(false)
    assert(!output.contains("\u001b"), "plain help should not contain escape codes")
    assert(output.contains("--color <auto|on|off>"), "should document --color flag")
    assert(output.contains("--version"), "should document --version flag")
    assert(output.contains(EmbeddedResources.version), "should contain version")
    assert(output.contains("https://github.com/github/melezov/server-banner"), "should contain project URL")

  test("colored help has ANSI escape codes"):
    val output = Main.help(true)
    assert(output.contains("\u001b["), "colored help should contain ANSI escape codes")
    assert(output.contains("--color <auto|on|off>"), "should document --color flag")
    assert(output.contains("--version"), "should document --version flag")
    assert(output.contains(Color.Yellow.ansiCode), "should contain yellow for headers")
    assert(output.contains(Color.Green.ansiCode), "should contain green for options")
    assert(output.contains(EmbeddedResources.version), "should contain version")
