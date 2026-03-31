package com.github.melezov.serverbanner

import scala.scalanative.posix.unistd

private[serverbanner] object Platform:
  def isatty(fd: Int): Boolean =
    unistd.isatty(fd) != 0
