package com.github.melezov.serverbanner

import org.specs2.execute.Result

class GreetingSpec extends BannerSpec {
  def is = s2"""
  Basics
    empty greeting  $testEmpty
    welcome         $testWelcome
    testWhitespace  $testWhitespace
"""

  // ### Basics ###

  def testEmpty: Result =
    Greeting("") === "\n"

  def testWelcome: Result =
    Greeting("Welcome to") === "W E L C O M E  T O\n"

  def testWhitespace: Result =
    Greeting("I am\tTAB\tto meet you!") === "I  A M   T A B   T O  M E E T  Y O U !\n"
}
