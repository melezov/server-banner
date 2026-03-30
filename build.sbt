enablePlugins(ScalaNativePlugin)

organization := "com.github.melezov"
name := "server-banner"
version := "0.0.4-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalameta" %%% "munit" % "1.1.0" % Test,
)

testFrameworks += new TestFramework("munit.Framework")

scalaVersion := "3.8.1"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-explain",
)

EmbedResources.settings

Global / onChangedBuildSource := ReloadOnSourceChanges
