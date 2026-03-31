import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import CustomKeys.release

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
      "-release:17",
    ),

    libraryDependencies += "org.scalameta" %%% "munit" % "1.1.0" % Test,
    testFrameworks += new TestFramework("munit.Framework"),

    Compile / mainClass := Some("com.github.melezov.serverbanner.Main"),

    Compile / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "main" / "scala",
    Test / unmanagedSourceDirectories += baseDirectory.value / ".." / "src" / "test" / "scala",

    EmbedResources.settings,
  )

lazy val root = project.in(file("."))
  .aggregate(target.jvm, target.native)
  .settings(
    name := "root",
    Compile / sources := Seq.empty,
    Test / sources := Seq.empty,
    publish / skip := true,
    release := {
      val jvmArtifact = (target.jvm / release).value
      val nativeArtifact = (target.native / release).value
      baseDirectory.value / "release"
    },
  )

onLoad in Global := (onLoad in Global).value.andThen("project targetNative" :: _)
Global / onChangedBuildSource := ReloadOnSourceChanges
