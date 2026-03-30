package com.github.melezov.serverbanner

object Greeting:
  private val transform = Map(
    ' ' -> " ",
    '\t' -> "  ",
  ).withDefault(ch => s"${ch.toUpper} ")

  def apply(text: String): String =
    val sb = StringBuilder()
    for ch <- text do
      sb ++= transform(ch)
    sb.toString.trim + "\n"
