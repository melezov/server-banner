organization := "com.github.melezov"
name := "server-banner"
version := "0.0.4-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.specs2"                 %% "specs2-core"     % "5.6.4"  % Test,
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"  % Test,
  "ch.qos.logback"             %  "logback-classic" % "1.5.32" % Test,
)

scalaVersion := "3.8.2"
scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-explain",
)

Global / onChangedBuildSource := ReloadOnSourceChanges
