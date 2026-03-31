import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scala.scalanative.build.*

ThisBuild / scalaVersion := "3.8.1"

val releaseDir = file("release")
val release = taskKey[File]("Build release artifact and copy to release/")

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
      "-release:17",
    ),

    libraryDependencies += "org.scalameta" %%% "munit" % "1.1.0" % Test,
    testFrameworks += new TestFramework("munit.Framework"),

    Compile / mainClass := Some("com.github.melezov.serverbanner.Main"),

    Compile / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "main" / "scala",
    Test / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "test" / "scala",

    EmbedResources.settings,
  )
  .jvmEnablePlugins(SbtProguard)
  .jvmSettings(
    assembly / mainClass := Some("com.github.melezov.serverbanner.Main"),
    assembly / assemblyJarName := "server-banner.jar",
    assembly / assemblyMergeStrategy := {
      case PathList(ps @ _*) if ps.last.endsWith(".tasty") => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value(x)
    },

    Proguard / proguardVersion := "7.6.1",
    Proguard / proguardOptions ++= Seq(
      ProguardOptions.keepMain("com.github.melezov.serverbanner.Main"),
      "-dontnote",
      "-dontwarn",
      "-dontobfuscate",
      "-dontoptimize",
      "-keep class scala.runtime.** { *; }",
      "-keepclassmembers class * extends java.lang.Enum { *; }",
      "-keepclassmembers class * { ** MODULE$; }",
    ),
    Proguard / proguardInputFilter := { _ => None },
    Proguard / proguardInputs := Seq(assembly.value),
    Proguard / proguardLibraries := Seq(
      file(System.getProperty("java.home") + "/jmods/java.base.jmod")
    ),

    release := {
      val log = streams.value.log
      val shrunk = (Proguard / proguard).value.head
      val dest = releaseDir / "server-banner.jar"
      IO.copyFile(shrunk, dest)
      log.success(s"Release JAR: ${dest.getAbsolutePath}")
      dest
    },
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withMode(Mode.debug)
        .withLTO(LTO.none)
        .withGC(GC.immix)
    },

    release := {
      val log = streams.value.log
      val s = state.value
      val extracted = Project.extract(s)
      val releaseState = extracted.appendWithoutSession(Seq(
        nativeConfig ~= { _.withMode(Mode.releaseSize).withLTO(LTO.thin) }
      ), s)
      val releaseExtracted = Project.extract(releaseState)
      val (_, binary) = releaseExtracted.runTask(Compile / nativeLink, releaseState)

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

      val dest = releaseDir / binary.getName
      IO.copyFile(binary, dest)
      log.success(s"Release binary: ${dest.getAbsolutePath}")
      dest
    },
  )

Compile / sources := Seq.empty
Test / sources := Seq.empty

onLoad in Global := (onLoad in Global).value.andThen("project targetNative" :: _)
Global / onChangedBuildSource := ReloadOnSourceChanges
