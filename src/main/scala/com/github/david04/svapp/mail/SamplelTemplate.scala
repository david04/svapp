package com.github.david04.svapp.mail


class SampleTemplate() extends EmailTemplate {

  def subject(): String = "SUBJECT"

  def preview(): String = subject()

  def mainTitle(): String = "TITLE"

  def onlineSrc(): Option[String] = Some("http://www.google.com")

  def sideContent(): String = "SIDE CONTENT"

  def mainContent(): String = "MAIN CONTENT"
}
