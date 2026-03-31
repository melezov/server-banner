package com.github.melezov.serverbanner

import scala.collection.mutable.ArrayBuffer

object Banner:
  val DefaultGreeting = "Scala  Native  Server  MOTD  generator"
  val DefaultBannerText = "server-banner"

  def render(bannerText: String, greeting: Option[String], color: Boolean): String =
    val slant = ColorText(Slant(bannerText), Color.Yellow)

    val canvas = greeting match
      case Some(text) =>
        val greetingCt = ColorText(Greeting(text), Color.Green)
        val bodyWidth = math.max(slant.width + 6, greetingCt.width - 2)
        val scroll = ColorText(Scroll(bodyWidth, slant.height), Color.Red)
        Canvas(scroll.width, scroll.height)
          .draw(Drawing(scroll, 0, 0, 0))
          .draw(Drawing(greetingCt, 8, 2, 1))
          .draw(Drawing(slant, 11, 4, 2))
      case None =>
        val scroll = ColorText(Scroll(slant.width + 6, slant.height), Color.Red)
        Canvas(scroll.width, scroll.height)
          .draw(Drawing(slant, 11, 4, 1))
          .draw(Drawing(scroll, 0, 0, 0))

    canvas.render(color)

// ### Model ###

enum Color:
  case Red, Green, Yellow

  def ansiCode: String = this match
    case Red    => "\u001b[31m"
    case Green  => "\u001b[92m"
    case Yellow => "\u001b[93m"

object Color:
  val AnsiReset = "\u001b[0m"

case class ColorText(text: String, color: Color):
  val lines: Seq[(String, Int)] = {
    val raw = text.split('\n').toIndexedSeq
    raw map { line =>
      val start = line.indexWhere(_ != ' ')
      if start < 0 then ("", 0) // should not happen
      else (line.drop(start), start)
    }
  }
  val height: Int = lines.length
  val width: Int = lines.map { case (line, start) => line.length + start }.max

case class Drawing(colorText: ColorText, x: Int, y: Int, z: Int)

class Canvas(val width: Int, val height: Int):
  private val drawings = ArrayBuffer[Drawing]()

  def draw(drawing: Drawing): this.type =
    drawings += drawing
    this

  def render(color: Boolean): String =
    val chars = Array.fill(width * height)(' ')
    val colors = new Array[String](width * height)

    for drawing <- drawings.sortBy(_.z) do
      for y <- 0 until drawing.colorText.height do
        val pY = y + drawing.y
        if pY >= 0 && pY < height then
          val (line, start) = drawing.colorText.lines(y)
          for x <- 0 until line.length do {
              val pX = x + drawing.x + start
              if pX >= 0 && pX < width then
                val ch = line(x)
                val idx = pX + pY * width
                chars(idx) = ch
                colors(idx) = drawing.colorText.color.ansiCode
          }

    if !color then
      val sb = StringBuilder()
      for y <- 0 until height do
        sb ++= new String(chars, y * width, width).replaceFirst(" *$", "\n")
      sb.toString
    else
      val sb = StringBuilder()
      var currentColor = Color.AnsiReset
      for y <- 0 until height do
        val lineStart = y * width
        var lastNonSpace = width - 1
        while lastNonSpace >= 0 && chars(lineStart + lastNonSpace) == ' ' do
          lastNonSpace -= 1
        for x <- 0 to lastNonSpace do
          val idx = lineStart + x
          val ch = chars(idx)
          val c = colors(idx)
          if c != null && c != currentColor then
            sb ++= c
            currentColor = c
          sb += ch
        sb += '\n'
      sb.setLength(sb.length - 1)
      sb ++= Color.AnsiReset += '\n'
      sb.toString

  override def toString: String = render(color = false)
end Canvas
