package com.github.melezov.serverbanner

import scala.collection.mutable.ArrayBuffer

object Banner:
  val DefaultGreeting = "Scala  Native  Server  MOTD  generator"
  val DefaultBannerText = "server-banner"

  def render(bannerText: String, greeting: Option[String], color: Boolean): String =
    val slant = ColorText(Slant(bannerText), Color.Yellow)
    val scroll = ColorText(Scroll(slant.width + 6, slant.height), Color.Red)

    val canvas = greeting match
      case Some(text) =>
        val greetingCt = ColorText(Greeting(text), Color.Green)
        Canvas(scroll.width, scroll.height)
          .draw(Drawing(greetingCt, 8, 2, 0))
          .draw(Drawing(slant, 11, 4, 1))
          .draw(Drawing(scroll, 0, 0, 0))
      case None =>
        Canvas(scroll.width, scroll.height)
          .draw(Drawing(slant, 11, 4, 1))
          .draw(Drawing(scroll, 0, 0, 0))

    canvas.render(color)

// ### Model ###

enum Color:
  case Red, Green, Yellow

  def ansiCode: String = this match
    case Red    => "\u001b[31m"
    case Green  => "\u001b[32m"
    case Yellow => "\u001b[33m"

object Color:
  val AnsiReset = "\u001b[0m"

case class ColorText(text: String, color: Color):
  val lines: Seq[String] = text.split('\n').toIndexedSeq
  val height: Int = lines.length
  val width: Int = lines.map(_.length).max

case class Drawing(colorText: ColorText, x: Int, y: Int, z: Int)

class Canvas(val width: Int, val height: Int):
  private val drawings = ArrayBuffer[Drawing]()

  def draw(drawing: Drawing): this.type =
    drawings += drawing
    this

  def render(color: Boolean): String =
    val chars = Array.fill(width * height)(' ')
    val colors = new Array[Color](width * height)

    for drawing <- drawings.sortBy(_.z) do
      for y <- 0 until drawing.colorText.height do
        val pY = y + drawing.y
        if pY >= 0 && pY < height then
          for x <- 0 until drawing.colorText.width do
            if x < drawing.colorText.lines(y).length then
              val pX = x + drawing.x
              if pX >= 0 && pX < width then
                val ch = drawing.colorText.lines(y)(x)
                if ch != ' ' then
                  val idx = pX + pY * width
                  chars(idx) = ch
                  colors(idx) = drawing.colorText.color

    if !color then
      val sb = StringBuilder()
      for y <- 0 until height do
        sb ++= new String(chars, y * width, width).replaceFirst(" *$", "\n")
      sb.toString
    else
      val sb = StringBuilder()
      var currentColor: Color | Null = null
      for y <- 0 until height do
        val lineStart = y * width
        var lastNonSpace = width - 1
        while lastNonSpace >= 0 && chars(lineStart + lastNonSpace) == ' ' do
          lastNonSpace -= 1
        for x <- 0 to lastNonSpace do
          val idx = lineStart + x
          val ch = chars(idx)
          if ch != ' ' then
            val c = colors(idx)
            if c != currentColor then
              sb ++= c.ansiCode
              currentColor = c
          sb += ch
        sb += '\n'
      if currentColor != null then
        sb ++= Color.AnsiReset
      sb.toString

  override def toString: String = render(color = false)
end Canvas
