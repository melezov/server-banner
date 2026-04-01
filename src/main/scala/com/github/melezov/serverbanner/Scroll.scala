package com.github.melezov.serverbanner

object Scroll:
  private val template: String = EmbeddedResources.scrollTemplate

  private final val Row = "\n|       |"

  private def factory(height: Int): String = height match
    case 1 => template
      .replace("x", "")
      .replace("y", "")
      .replace("z", "")
    case 2 => template
      .replace("u/", "s |")
      .replace("x", "u/")
      .replace("y", "")
      .replace("z", "")
    case 3 => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "u/")
      .replace("z", Row)
    case 4 => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "s |")
      .replace("z", "u/" + Row)
    case x => template
      .replace("u/", "s |")
      .replace("x", "s |")
      .replace("y", "s |")
      .replace("z", ("s |" + Row) * (x - 4) + "u/" + Row)

  def apply(bodyWidth: Int, bodyHeight: Int): String =
    require(bodyWidth > 0, "Scroll body width must be positive, got: " + bodyWidth)
    require(bodyHeight > 0, "Scroll body height must be positive, got: " + bodyHeight)

    val bufferSize = 256
    val spaces = Array.fill(bufferSize)(' ')
    val underscores = Array.fill(bufferSize)('_')

    val sb = java.lang.StringBuilder(1024)
    for ch <- factory(bodyHeight) do
      if ch == 's' || ch == 'u' then
        val rep = if ch == 's' then spaces else underscores
        var i = bodyWidth - 1 // template already includes the first column
        while i > 0 do
          val chunkSize = math.min(i, bufferSize)
          sb.append(rep, 0, chunkSize)
          i -= chunkSize
      else
        sb.append(ch)
    sb.toString
