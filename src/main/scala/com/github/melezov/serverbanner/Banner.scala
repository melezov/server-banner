package com.github.melezov.serverbanner

import scala.collection.mutable.ArrayBuffer

object Banner:
  val DefaultGreeting = "Scala  Native  Server  MOTD  generator"
  val DefaultBannerText = "server-banner"

  def render(bannerText: String, greeting: Option[String]): String =
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

    canvas.toString

// ### Model ###

enum Color:
  case Red, Yellow, Green

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

  override def toString: String =
    val buffer = Array.fill(width * height)(' ')

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
                  buffer(pX + pY * width) = ch

    val sb = StringBuilder()
    for y <- 0 until height do
      sb ++= new String(buffer, y * width, width).replaceFirst(" *$", "\n")
    sb.toString
end Canvas
