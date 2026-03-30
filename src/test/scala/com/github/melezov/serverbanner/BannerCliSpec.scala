package com.github.melezov.serverbanner

class BannerCliSpec extends BannerSuite:

  // ### Argument parsing ###

  test("no arguments returns None"):
    assertEquals(Main.parseArgs(Array.empty), Right(None))

  test("banner text only"):
    assertEquals(Main.parseArgs(Array("my-app")), Right(Some(Main.Config("my-app", None))))

  test("banner text with --greeting"):
    assertEquals(
      Main.parseArgs(Array("--greeting", "Welcome to", "my-app")),
      Right(Some(Main.Config("my-app", Some("Welcome to")))),
    )

  test("banner text with -g shorthand"):
    assertEquals(
      Main.parseArgs(Array("-g", "Hello", "my-app")),
      Right(Some(Main.Config("my-app", Some("Hello")))),
    )

  test("greeting after banner text"):
    assertEquals(
      Main.parseArgs(Array("my-app", "--greeting", "Welcome")),
      Right(Some(Main.Config("my-app", Some("Welcome")))),
    )

  // ### Error cases ###

  test("--greeting without value"):
    assert(Main.parseArgs(Array("--greeting")).isLeft)

  test("unknown option"):
    assert(Main.parseArgs(Array("--unknown", "my-app")).isLeft)

  test("missing banner text"):
    assert(Main.parseArgs(Array("--greeting", "Hello")).isLeft)

  test("extra positional argument"):
    assert(Main.parseArgs(Array("my-app", "extra")).isLeft)

  // ### Rendering ###

  test("render with banner text only"):
    val output = Banner.render("test", None)
    assert(output.contains("/"), "should contain scroll decoration")
    assert(output.nonEmpty)

  test("render with greeting"):
    val output = Banner.render("test", Some("Hello"))
    assert(output.contains("H E L L O"), "should contain spaced greeting")

  test("default banner renders"):
    val output = Banner.render("server-banner", Some("Pure  Scala  Server  MOTD  generator"))
    assert(output.contains(".---."), "should contain scroll decoration")
    assert(output.contains("P U R E"), "should contain greeting")
