package com.github.melezov.serverbanner

class BannerCliSpec extends BannerSuite:

  // ### Argument parsing ###

  test("no arguments returns None"):
    assertEquals(Main.parseArgs(Array.empty), Right(None))

  test("banner text only"):
    assertEquals(Main.parseArgs(Array("HT-California-02")), Right(Some(Main.Config("HT-California-02", None))))

  test("banner text with --greeting"):
    assertEquals(
      Main.parseArgs(Array("--greeting", "Plenty of room at the", "HT-California-02")),
      Right(Some(Main.Config("HT-California-02", Some("Plenty of room at the")))),
    )

  test("banner text with -g shorthand"):
    assertEquals(
      Main.parseArgs(Array("-g", "Such a lovely place", "HT-California-02")),
      Right(Some(Main.Config("HT-California-02", Some("Such a lovely place")))),
    )

  test("greeting after banner text"):
    assertEquals(
      Main.parseArgs(Array("HT-California-02", "--greeting", "You can check out any time you like")),
      Right(Some(Main.Config("HT-California-02", Some("You can check out any time you like")))),
    )

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
    val output = Banner.render("HT-California-02", None)
    assert(output.contains(".---."), "should contain scroll decoration")
    assert(output.nonEmpty)

  test("render with greeting"):
    val output = Banner.render("HT-California-02", Some("Such a lovely place"))
    assert(output.contains("S U C H  A  L O V E L Y  P L A C E"), "should contain spaced greeting")

  test("default banner renders"):
    val output = Banner.render(Banner.DefaultBannerText, Some(Banner.DefaultGreeting))
    assert(output.contains(".---."), "should contain scroll decoration")
    assert(output.contains("S C A L A"), "should contain greeting")
