package com.github.melezov.serverbanner

class GreetingSpec extends BannerSuite:

  test("empty greeting"):
    assertEquals(Greeting(""), "\n")

  test("welcome"):
    assertEquals(Greeting("Welcome to"), "W E L C O M E  T O\n")

  test("whitespace"):
    assertEquals(Greeting("I am\tTAB\tto meet you!"), "I  A M   T A B   T O  M E E T  Y O U !\n")
