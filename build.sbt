enablePlugins(ScalaNativePlugin)

import scala.scalanative.build.*

nativeConfig ~= {
  _.withMode(Mode.debug)
    .withLTO(LTO.none)
    .withGC(GC.immix)
}

val upxPack = taskKey[File]("Compress the native binary with UPX")
upxPack := {
  val binary = (Compile / nativeLink).value
  import scala.sys.process.*
  val log = streams.value.log
  log.info(s"UPX compressing ${binary.getName} ...")
  val output = Process(Seq("upx", "--best", "--force", binary.getAbsolutePath)).!!
  log.info(output.trim)
  binary
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
