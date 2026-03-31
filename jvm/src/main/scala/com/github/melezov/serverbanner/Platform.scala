package com.github.melezov.serverbanner

private[serverbanner] object Platform:
  def isatty(fd: Int): Boolean =
    System.console() != null
