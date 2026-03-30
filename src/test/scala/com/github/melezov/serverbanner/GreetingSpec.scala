package com.github.melezov.serverbanner

class GreetingSpec extends BannerSuite:

  // ### Basics ###

  test("empty greeting"):
    assertEquals(Greeting(""), "\n")

  test("welcome"):
    assertEquals(Greeting("Welcome to"), "W E L C O M E  T O\n")

  test("multiple spaces"):
    assertEquals(Greeting("Some dance  to    remember"), "S O M E  D A N C E   T O     R E M E M B E R\n")

  test("tab"):
    assertEquals(Greeting("Some dance\tto\tforget"), "S O M E  D A N C E   T O   F O R G E T\n")
