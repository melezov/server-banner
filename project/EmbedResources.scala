import sbt.*
import sbt.Keys.*

object EmbedResources {
  private def quoted(s: String): String = {
    val sb = new StringBuilder(s.length * 2 + 2)
    sb += '"'
    s foreach {
      case '\\' => sb ++= "\\\\"
      case '"'  => sb ++= "\\\""
      case '\n' => sb ++= "\\n"
      case '\r' => sb ++= "\\r"
      case '\t' => sb ++= "\\t"
      case c if c < ' ' => sb ++= f"\\u${c.toInt}%04x"
      case c    => sb += c
    }
    sb += '"'
    sb.toString
  }

  val settings: Seq[Setting[_]] = Seq(
    Compile / sourceGenerators += Def.task {
      val resourceDir = (ThisBuild / baseDirectory).value / "src" / "main" / "resources" / "com" / "github" / "melezov" / "serverbanner"
      val outputDir = (Compile / sourceManaged).value / "com" / "github" / "melezov" / "serverbanner"
      outputDir.mkdirs()

      val scrollContent = IO.read(resourceDir / "scroll.txt")
      val slantContent = IO.read(resourceDir / "slant.txt")
      val projectVersion = version.value

      val chunkSize = 30000
      val chunks = slantContent.grouped(chunkSize).toIndexedSeq

      val sb = new StringBuilder
      sb ++= "package com.github.melezov.serverbanner\n\n" ++=
        "private[serverbanner] object EmbeddedResources:\n" ++=
        s"  val version: String =\n    ${quoted(projectVersion)}\n\n" ++=
        s"  val scrollTemplate: String =\n    ${quoted(scrollContent)}\n\n"

      for ((chunk, i) <- chunks.zipWithIndex) {
        sb ++= s"  private val s$i: String =\n    ${quoted(chunk)}\n\n"
      }
      sb ++= s"  val slantBuffer: Array[Char] =\n    (${chunks.indices.map(i => s"s$i").mkString(" + ")}).toArray\n"

      val outFile = outputDir / "EmbeddedResources.scala"
      IO.write(outFile, sb.toString)
      Seq(outFile)
    }
  )
}
