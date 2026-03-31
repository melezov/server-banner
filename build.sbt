import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.scalanative.build.*

ThisBuild / scalaVersion := "3.8.1"

lazy val target = crossProject(NativePlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    organization := "com.github.melezov",
    name := "server-banner",
    version := "0.1.0-SNAPSHOT",

    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-explain",
    ),

    libraryDependencies += "org.scalameta" %%% "munit" % "1.1.0" % Test,
    testFrameworks += new TestFramework("munit.Framework"),

    Compile / mainClass := Some("com.github.melezov.serverbanner.Main"),

    Compile / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "main" / "scala",
    Test / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "test" / "scala",

    EmbedResources.settings,
  )
  .jvmSettings(
    assembly / mainClass := Some("com.github.melezov.serverbanner.Main"),
    assembly / assemblyJarName := "server-banner.jar",
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withMode(Mode.debug)
        .withLTO(LTO.none)
        .withGC(GC.immix)
    },

    commands += Command.command("release") { state0 =>
      val extracted = Project.extract(state0)
      val releaseState = extracted.appendWithoutSession(Seq(
        nativeConfig ~= { _.withMode(Mode.releaseSize).withLTO(LTO.thin) }
      ), state0)

      val log = extracted.get(sLog)
      val releaseExtracted = Project.extract(releaseState)
      val (state1, binary) = releaseExtracted.runTask(Compile / nativeLink, releaseState)

      try {
        log.info(s"UPX compressing ${binary.getName} ...")
        import scala.sys.process.Process
        val output = Process(Seq("upx", "--best", binary.getAbsolutePath)).!!
        log.info(output.trim)
      } catch {
        case e: java.io.IOException =>
          log.warn("UPX not found on PATH, skipping compression")
        case e: RuntimeException =>
          log.warn(s"UPX failed: ${e.getMessage.linesIterator.next()}")
      }

      val releaseDir = extracted.get(baseDirectory) / "release"
      val dest = releaseDir / binary.getName
      sbt.IO.copyFile(binary, dest)
      log.success(s"Release binary: ${dest.getAbsolutePath}")

      state1
    },
  )

Compile / sources := Seq.empty
Test / sources := Seq.empty

onLoad in Global := (onLoad in Global).value.andThen("project targetNative" :: _)
Global / onChangedBuildSource := ReloadOnSourceChanges
