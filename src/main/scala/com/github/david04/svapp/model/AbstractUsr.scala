package com.github.david04.svapp.model

import java.util.{TimeZone, Calendar}
import java.util.Date
import scala.Some
import anorm.SqlRow
import java.math.BigInteger
import java.security.MessageDigest
import com.github.david04.svapp.base.{SVAppDB, SVApp}


trait DBUsrSVAppComponent {
  svApp: SVAppDB =>

  abstract class AbstractUsr()(implicit row: SqlRow) extends DBObjectClassTrait {

    // Properties
    val id = immutableVal[Int]("id")
    val account: DBObjProp[_ <: AbstractAccount]
    val email = mutableValI[String]("email")
    val password = mutableValI[String]("password")
    val firstname = mutableValI[String]("firstname")
    val lastname = mutableValI[String]("lastname")
    val timezone = mutableValI[String]("timezone")

    def name = firstname.value + " " + lastname.value

    // TimeZone Manipulation
    private def timeOffset(date: Date): (Int, Int) = {
      val systemTimezone = Calendar.getInstance().getTimeZone
      val userTimezone = TimeZone.getTimeZone(timezone.value)

      val systemOffset: Int = systemTimezone.getOffset(date.getTime)
      val userOffset: Int = userTimezone.getOffset(date.getTime)

      (userOffset, systemOffset)
    }

    def sysToUserTime(date: Date): Date = {
      val offset = timeOffset(date)
      val userCal = Calendar.getInstance()
      userCal.setTime(date)
      userCal.add(Calendar.MILLISECOND, offset._1)
      userCal.add(Calendar.MILLISECOND, -offset._2)

      userCal.getTime
    }

    def userToSysTime(date: Date): Date = {
      val offset = timeOffset(date)
      val sysCal = Calendar.getInstance()
      sysCal.setTime(date)
      sysCal.add(Calendar.MILLISECOND, -offset._1)
      sysCal.add(Calendar.MILLISECOND, offset._2)

      sysCal.getTime
    }

    def now = sysToUserTime(new Date)
  }

  abstract class AbstractUsrs[T <: AbstractUsr] extends DBCompanionObjectTrait[T] {


    def md5(input: String) = {
      val digest = MessageDigest.getInstance("MD5");
      digest.update(input.getBytes(), 0, input.length());
      new BigInteger(1, digest.digest()).toString(16);
    }

    lazy val table = "usr"

    def fromEmail(email: String): Option[T] = where("email", email).headOption

    def authenticate(email: String, password: String): Boolean =
      fromEmail(email) match {case Some(u) => u.password.value == password case _ => false }
  }

}