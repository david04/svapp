package com.github.david04.svapp.util

import org.apache.log4j.Logger

trait Logging {
  self =>
  val log = Logger.getLogger(self.getClass)
}
