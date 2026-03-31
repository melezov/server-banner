import CustomKeys.release

enablePlugins(SbtProguard)

assembly / mainClass := Some("com.github.melezov.serverbanner.Main")
assembly / assemblyJarName := "server-banner.jar"
assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last.endsWith(".tasty") => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value(x)
}

Proguard / proguardVersion := "7.6.1"
Proguard / proguardOptions ++= Seq(
  ProguardOptions.keepMain("com.github.melezov.serverbanner.Main"),
  "-dontnote",
  "-dontwarn",
  "-dontobfuscate",
  "-dontoptimize",
  "-keep class scala.runtime.** { *; }",
  "-keepclassmembers class * extends java.lang.Enum { *; }",
  "-keepclassmembers class * { ** MODULE$; }",
)
Proguard / proguardInputFilter := { _ => None }
Proguard / proguardInputs := Seq(assembly.value)
Proguard / proguardLibraries := Seq(
  file(System.getProperty("java.home") + "/jmods/java.base.jmod")
)

release := {
  val log = streams.value.log
  val shrunk = (Proguard / proguard).value.head
  val dest = (ThisBuild / baseDirectory).value / "release" / "server-banner.jar"
  IO.copyFile(shrunk, dest)
  log.success(s"Release JAR: ${dest.getAbsolutePath}")
  dest
}
