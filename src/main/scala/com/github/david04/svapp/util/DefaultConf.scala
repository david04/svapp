package com.github.david04.svapp.util

import org.apache.log4j.Level
import java.io.File
import com.github.david04.svapp.base.SVApp
import com.typesafe.config.{Config, ConfigFactory}

trait ConfSVAppComponent {
  svApp: SVApp =>

  val conf: DefaultConf

  abstract class DefaultConf extends Logging {

    protected lazy val config: Config = ConfigFactory.load()

    protected def get[T](key: String, default: Option[T], parser: String => T) =
      if (config.hasPath(key))
        try {
          parser(config.getString(key))
        } catch {
          case e: Exception => default match {
            case Some(d) => {log.error("Exception while parsing config key '" + key + "', using default ('" + d + "')"); d}
            case None => throw new RuntimeException("Exception while parsing config key '" + key + "', and not default is available")
          }
        }
      else
        default match {
          case Some(d) => {log.error("No value found for configuration key '" + key + "', using default ('" + d + "')"); d}
          case None => throw new RuntimeException("No value found for configuration key '" + key + "', and no default is available")
        }

    protected def boolean(key: String, default: Boolean) = get(key, Some(default), _.toBoolean)

    protected def int(key: String, default: Int) = get(key, Some(default), _.toInt)

    protected def string(key: String, default: String) = get(key, Some(default), identity[String])

    protected def string(key: String) = get[String](key, Option.empty[String], identity[String])

    protected def file(key: String, default: File) = get(key, Some(default), s => new File(s))

    private def noLeadingSlash(str: String) = str match {case s if s.last == '/' => s.substring(0, s.length - 2); case s => s }

    // Domain Options
    val appDomain = noLeadingSlash(string("appDomain", "localhost"))

    // Logging Options
    val logging = boolean("logging", true)
    val consoleLogging = Level.toLevel(string("consoleLogging", "OFF"))
    val logPath = file("logPath", new File("./default.log"))
    val supportEmail = string("supportEmail", "davidbranquinho@gmail.com")

    // Email Options
    val emailFrom = string("emailFrom", "davidbranquinho@gmail.com")
    val emailPassword = string("emailPassword", "")
    val emailSmtpAuth = boolean("smtpAuth", false)
    val emailSmtpStarttls = boolean("smtpStarttls", false)
    val emailSmtpHost = string("smtpHost", "localhost")
    val emailSmtpPort = int("smtpPort", 25)

    // Database Options
    val dbname = string("dbname")
    val dbuser = string("dbuser")
    val dbpass = string("dbpass", "")

    val dev = boolean("dev", false)
  }

}