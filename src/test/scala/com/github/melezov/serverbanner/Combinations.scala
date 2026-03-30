package com.github.melezov.serverbanner

import scala.annotation.tailrec

class Combinations(chars: String, depth: Int) extends IndexedSeq[String] {
  val length: Int = {
    var sum = 0
    var level = 0
    while (level < depth) {
      sum = (sum + 1) * chars.length
      level += 1
    }
    sum
  }

  @tailrec
  private def resolve(sb: StringBuilder, length: Long, index: Long): String = {
    val chunk = length / chars.length
    val div = index / chunk
    sb += chars(div.toInt)
    val mul = div * chunk
    if (mul == index) {
      sb.toString
    } else {
      resolve(sb, chunk - 1, index - mul - 1)
    }
  }

  def apply(idx: Int): String =
    resolve(new StringBuilder, length, idx)
}

/** Creates an input file for a Slant generator like Figlet / Toilet */
object Combinations {
  def main(args: Array[String]): Unit = {
    val Level = 3
    val Chars = Slant.AllowedChars.mkString.sorted

    val os = new java.io.BufferedOutputStream(new java.io.FileOutputStream(s"$Level-combinations.txt"))
    new Combinations(Chars, Level) foreach { line =>
      os.write(line.getBytes("ISO-8859-1"))
      os.write('\n')
    }
    os.close()
  }
}
