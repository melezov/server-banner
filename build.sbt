enablePlugins(ScalaNativePlugin)

import scala.scalanative.build.*

nativeConfig ~= {
  _.withMode(Mode.debug)
    .withLTO(LTO.none)
    .withGC(GC.immix)
}

commands += Command.command("release") { state0 =>
  val extracted = Project.extract(state0)
  val releaseState = extracted.appendWithoutSession(Seq(
    nativeConfig ~= { _.withMode(Mode.releaseSize).withLTO(LTO.thin) }
  ), state0)

  val log = extracted.get(sLog)
  val releaseExtracted = Project.extract(releaseState)
  val (state1, binary) = releaseExtracted.runTask(Compile / nativeLink, releaseState)

  {
    import scala.sys.process._
    try {
      log.info(s"UPX compressing ${binary.getName} ...")
      val output = Process(Seq("upx", "--best", binary.getAbsolutePath)).!!
      log.info(output.trim)
    } catch {
      case e: java.io.IOException =>
        log.warn("UPX not found on PATH, skipping compression")
      case e: RuntimeException =>
        log.warn(s"UPX failed: ${e.getMessage.linesIterator.next()}")
    }
  }

  val releaseDir = extracted.get(baseDirectory) / "release"
  sbt.IO.createDirectory(releaseDir)
  val dest = releaseDir / binary.getName
  sbt.IO.copyFile(binary, dest)
  log.success(s"Release binary: ${dest.getAbsolutePath}")

  state1
}

organization := "com.github.melezov"
name := "server-banner"
version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalameta" %%% "munit" % "1.1.0" % Test,
)

testFrameworks += new TestFramework("munit.Framework")

scalaVersion := "3.8.1"
Compile / mainClass := Some("com.github.melezov.serverbanner.Main")
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-explain",
)

EmbedResources.settings

Global / onChangedBuildSource := ReloadOnSourceChanges
