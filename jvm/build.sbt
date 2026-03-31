import CustomKeys.release

enablePlugins(SbtProguard)

clean := {
  clean.value
  val dest = (ThisBuild / baseDirectory).value / "release" / "server-banner.jar"
  if (dest.exists()) IO.delete(dest)
}

assembly / mainClass := Some("com.github.melezov.serverbanner.Main")
assembly / assemblyJarName := "server-banner.jar"
assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps.last.endsWith(".tasty") => MergeStrategy.discard
  case x => (assembly / assemblyMergeStrategy).value(x)
}

Proguard / proguardVersion := "7.9.0"
Proguard / proguardOptions ++= Seq(
  ProguardOptions.keepMain("com.github.melezov.serverbanner.Main"),
  "-dontnote",
  "-dontwarn",
  "-optimizationpasses 5",
  "-overloadaggressively",
  "-mergeinterfacesaggressively",
  "-dontusemixedcaseclassnames",
  "-allowaccessmodification",
  "-repackageclasses 'com.github.melezov.serverbanner'",
  // LazyVals uses MethodHandles.lookup - allow repackaging but keep members
  "-keepclassmembers class scala.runtime.LazyVals { *; }",
  "-keepclassmembers class scala.runtime.LazyVals$ { *; }",
  "-keepclassmembers class * extends java.lang.Enum { *; }",
  "-keepclassmembers class * { ** MODULE$; }",
  // Scala 3 lazy vals use VarHandle to access fields by name
  "-keepclassmembers class * { *** *$lzy*; }",
)
Proguard / proguardInputFilter := { _ => Some("!library.properties") }
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
