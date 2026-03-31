import CustomKeys.release

import scala.scalanative.build.*

clean := {
  clean.value
  val ext = if (System.getProperty("os.name", "").startsWith("Windows")) ".exe" else ""
  val dest = (ThisBuild / baseDirectory).value / "release" / (name.value + ext)
  if (dest.exists()) IO.delete(dest)
}

nativeConfig ~= {
  _.withMode(Mode.debug)
    .withLTO(LTO.none)
    .withGC(GC.immix)
}

release := {
  val log = streams.value.log
  val ref = thisProjectRef.value
  val s = state.value
  val extracted = Project.extract(s)
  val releaseState = extracted.appendWithoutSession(Seq(
    nativeConfig ~= { _.withMode(Mode.releaseSize).withLTO(LTO.thin) }
  ), s)
  val releaseExtracted = Project.extract(releaseState)
  val (_, binary) = releaseExtracted.runTask(ref / (Compile / nativeLink), releaseState)

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

  val dest = (ThisBuild / baseDirectory).value / "release" / binary.getName
  IO.copyFile(binary, dest)
  log.success(s"Release binary: ${dest.getAbsolutePath}")
  dest
}
