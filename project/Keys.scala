import sbt._

object CustomKeys {
  val release = taskKey[File]("Build release artifact and copy to release/")
}
