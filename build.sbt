import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / scalaVersion := "3.8.1"

import CustomKeys.release

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

Compile / sources := Seq.empty
Test / sources := Seq.empty

onLoad in Global := (onLoad in Global).value.andThen("project targetNative" :: _)
Global / onChangedBuildSource := ReloadOnSourceChanges
