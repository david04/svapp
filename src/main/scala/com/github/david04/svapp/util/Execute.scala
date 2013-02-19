package com.github.david04.svapp.util

import io.Source

object Execute {

  def apply(command: String): Seq[String] = {
    val p = Runtime.getRuntime().exec(Array[String]("bash", "-c", command))
    p.waitFor()
    val in = Source.createBufferedSource(p.getInputStream)
    try {in.getLines().toSeq} finally {in.close()}
  }
}
