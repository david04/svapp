package com.github.david04.svapp.mail


abstract class EmailTemplate {

  def subject(): String

  def preview(): String

  def mainTitle(): String

  def onlineSrc(): Option[String]

  def sideContent(): String

  def mainContent(): String

  def shouldSend(): Boolean = true
}
