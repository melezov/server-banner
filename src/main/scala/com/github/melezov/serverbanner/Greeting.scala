package com.github.melezov.serverbanner

object Greeting {
  private[this] val transform = Map(
    ' ' -> " "
  , '\t' -> "  "
  ).withDefault(ch => s"${ch.toUpper} ")

  def apply(text: String): String = {
    val sb = new StringBuilder
    for (ch <- text) {
      sb ++= transform(ch)
    }
    sb.toString.trim + "\n"
  }
}
