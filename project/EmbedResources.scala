import sbt._
import sbt.Keys._

object EmbedResources {
  private def escape(s: String): String = {
    val sb = new java.lang.StringBuilder(s.length * 2)
    var i = 0
    while (i < s.length) {
      s.charAt(i) match {
        case '\\' => sb.append("\\\\")
        case '"'  => sb.append("\\\"")
        case '\n' => sb.append("\\n")
        case '\r' => sb.append("\\r")
        case '\t' => sb.append("\\t")
        case c if c < ' ' => sb.append(f"\\u${c.toInt}%04x")
        case c    => sb.append(c)
      }
      i += 1
    }
    sb.toString
  }

  val settings: Seq[Setting[_]] = Seq(
    Compile / sourceGenerators += Def.task {
      val resourceDir = (Compile / resourceDirectory).value / "com" / "github" / "melezov" / "serverbanner"
      val outputDir = (Compile / sourceManaged).value / "com" / "github" / "melezov" / "serverbanner"
      outputDir.mkdirs()

      val scrollContent = IO.read(resourceDir / "scroll.txt")
      val slantContent = IO.read(resourceDir / "slant.txt")
      val projectVersion = version.value

      val outFile = outputDir / "EmbeddedResources.scala"
      val q = "\""

      val sb = new java.lang.StringBuilder()
      sb.append("package com.github.melezov.serverbanner\n\n")
      sb.append("private[serverbanner] object EmbeddedResources:\n")

      sb.append("  val version: String =\n    ")
      sb.append(q)
      sb.append(escape(projectVersion))
      sb.append(q)
      sb.append("\n\n")

      sb.append("  val scrollTemplate: String =\n    ")
      sb.append(q)
      sb.append(escape(scrollContent))
      sb.append(q)
      sb.append("\n\n")

      val chunkSize = 30000
      val chunks = slantContent.grouped(chunkSize).toIndexedSeq
      for ((chunk, i) <- chunks.zipWithIndex) {
        sb.append("  private val s" + i + ": String =\n    ")
        sb.append(q)
        sb.append(escape(chunk))
        sb.append(q)
        sb.append("\n\n")
      }

      sb.append("  val slantBuffer: Array[Char] =\n    (")
      sb.append(chunks.indices.map(i => "s" + i).mkString(" + "))
      sb.append(").toArray\n")

      IO.write(outFile, sb.toString)
      Seq(outFile)
    }
  )
}
