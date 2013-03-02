package com.github.david04.svapp.util

import java.util.{TimeZone, Date, GregorianCalendar, Calendar}
import java.text.SimpleDateFormat
import scala.Some
import com.github.david04.svapp.base.{SVAppDB, SVApp}

trait DateUtilsSVAppComponent {
  svApp: SVAppDB =>

  val dateUtil = new DateUtil

  implicit def toDatePlus(date: Date): DatePlus = new DatePlus(date)

  class DateUtil {

    val timezones = TimeZone.getAvailableIDs.filter(tz => {
      tz.matches("((\\w|-|_)+)/((\\w|-|_)+)") &&
        !tz.contains("Etc") &&
        !tz.contains("System") &&
        !tz.contains("Mideast")
    }).sortWith((s1, s2) => s1.compare(s2) < 0)

    private final val regexps = Map[String, String](
      ("^\\d{1}$", "H"),
      ("^\\d{2}$", "HH"),
      ("^\\d{2}\\s(pm|am)$", "HH a"),
      ("^\\d{4}$", "HHmm"),
      ("^\\d{2}:\\d{2}$", "HH:mm"),
      ("^\\d{4}\\s(pm|am)$", "HHmm a"),
      ("^\\d{6}$", "HHmmss"),
      ("^\\d{6}\\s(pm|am)$", "HHmmss a"),
      ("^\\d{1,2}:\\d{2}:\\d{2}$", "HH:mm:ss"),
      ("^\\d{1,2}:\\d{2}:\\d{2}\\s(pm|am)$", "HH:mm:ss a"),
      ("^\\d{1,2}\\s\\d{2}\\s\\d{2}$", "HH mm ss"),
      ("^\\d{1,2}\\s\\d{2}\\s\\d{2}\\s(pm|am)$", "HH mm ss a"))

    def toDate(s: String): Option[Date] =
      regexps.find(r => s.toLowerCase.matches(r._1)) match {
        case Some(regex) => try {
          Some(new SimpleDateFormat(regex._2).parse(s))
        } catch {
          case e => None
        }
        case _ => None
      }

    def formatDate(date: Date, style: Int = java.text.DateFormat.SHORT)(implicit usr: AbstractUsr): String = java.text.DateFormat.getDateInstance(style).format(usr.sysToUserTime(date))

    def today(implicit usr: AbstractUsr): Date = usr.sysToUserTime(dateOnly(new Date()))

    def yesterday(implicit usr: AbstractUsr): Date = usr.sysToUserTime(new Date(today.getTime - (1000 * 60 * 60 * 24)))

    def tomorrow(implicit usr: AbstractUsr): Date = usr.sysToUserTime(new Date(today.getTime + (1000 * 60 * 60 * 24)))

    def dateOnly(d: Date): Date = new Date(d.getTime - d.getTime % (1000 * 60 * 60 * 24))
  }

  trait SemanticDateVisitor[T] {

    def today(): T

    def yesterday(): T

    def tomorrow(): T

    def thisWeek(): T

    def lastWeek(): T

    def nextWeek(): T

    def thisMonth(): T

    def lastMonth(): T

    def nextMonth(): T

    def thisYear(): T

    def lastYear(): T

    def nextYear(): T

    def past(): T

    def future(): T
  }

  class DatePlus(date: Date) {
    val dateO = new Date(date.getTime - date.getTime % (1000 * 60 * 60 * 24))

    def formatDate(date: Date, style: Int = java.text.DateFormat.SHORT)(implicit usr: AbstractUsr): String = java.text.DateFormat.getDateInstance(style).format(usr.sysToUserTime(date))

    override def equals(other: Any): Boolean =
      (other.isInstanceOf[DatePlus] && other.asInstanceOf[DatePlus].dateO == dateO) || (other.isInstanceOf[Date] && other.asInstanceOf[Date] == dateO)

    override def hashCode: Int = dateO.hashCode()

    class CalendarPlus() extends GregorianCalendar(dateO.getYear + 1900, dateO.getMonth, dateO.getDate) {
      def ++(field: Int, amount: Int = 1) = {
        val n = clone().asInstanceOf[CalendarPlus]
        n.add(field, amount)
        n
      }

      def --(field: Int, amount: Int = 1) = {
        val n = clone().asInstanceOf[CalendarPlus]
        n.add(field, -amount)
        n
      }

      /** Field to max value */
      def max(field: Int) = field match {
        case Calendar.DAY_OF_WEEK => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.DAY_OF_WEEK).foreach(f => n.set(f, n.getActualMaximum(f)))
          n
        }
        case Calendar.DAY_OF_MONTH => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.DAY_OF_MONTH).foreach(f => n.set(f, n.getActualMaximum(f)))
          n
        }
        case Calendar.MONTH => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.MONTH).foreach(f => n.set(f, n.getActualMaximum(f)))
          n
        }
      }

      /** Field to min value */
      def min(field: Int) = field match {
        case Calendar.DAY_OF_WEEK => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.DAY_OF_WEEK).foreach(f => n.set(f, n.getActualMinimum(f)))
          n
        }
        case Calendar.DAY_OF_MONTH => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.DAY_OF_MONTH).foreach(f => n.set(f, n.getActualMinimum(f)))
          n
        }
        case Calendar.MONTH => {
          val n = clone().asInstanceOf[CalendarPlus]
          Seq(Calendar.MONTH).foreach(f => n.set(f, n.getActualMinimum(f)))
          n
        }
      }

      override def equals(other: Any): Boolean =
        super.equals(other) ||
          (other.isInstanceOf[DatePlus] && other.asInstanceOf[DatePlus].dateO == getTime) ||
          (other.isInstanceOf[Date] && other.asInstanceOf[Date] == getTime)

      implicit def toDatePlus = new DatePlus(getTime)
    }

    private def cal = new CalendarPlus()

    private def todayCal(implicit usr: AbstractUsr) = toDatePlus(dateUtil.today).cal

    def isToday(implicit usr: AbstractUsr) = date.equals(dateUtil.today)

    def isThisWeek(implicit usr: AbstractUsr) =
      ((date after dateUtil.today) && (date before (todayCal max Calendar.DAY_OF_WEEK).getTime)) ||
        ((date before dateUtil.today) && (date after (todayCal min Calendar.DAY_OF_WEEK).getTime))

    def isThisMonth(implicit usr: AbstractUsr) =
      ((date after dateUtil.today) && (date before (todayCal max Calendar.DAY_OF_MONTH).getTime)) ||
        ((date before dateUtil.today) && (date after (todayCal min Calendar.DAY_OF_MONTH).getTime))

    def isThisYear(implicit usr: AbstractUsr) =
      ((date after dateUtil.today) && (date before (todayCal max Calendar.MONTH max Calendar.DAY_OF_MONTH).getTime)) ||
        ((date before dateUtil.today) && (date after (todayCal min Calendar.MONTH min Calendar.DAY_OF_MONTH).getTime))

    def isInFuture(implicit usr: AbstractUsr) = (date after dateUtil.today)

    /* == */
    def isTomorrow(implicit usr: AbstractUsr) = (toDatePlus(dateUtil.today).todayCal ++ Calendar.DAY_OF_MONTH) == date

    /* == */
    def isYesterday(implicit usr: AbstractUsr) = date.equals(toDatePlus(dateUtil.today).todayCal -- Calendar.DAY_OF_MONTH)

    def accept[T](v: SemanticDateVisitor[T])(implicit usr: AbstractUsr) = {

      if (date.equals(dateUtil.today)) v.today()
      else if (date.after(dateUtil.today)) {

        import Calendar._

        val tomorrow = todayCal ++ DATE

        val week = todayCal max DAY_OF_WEEK

        val nextWeek = todayCal ++ WEEK_OF_YEAR max DAY_OF_WEEK

        val month = todayCal max DAY_OF_MONTH

        val nextMonth = todayCal ++ MONTH max DAY_OF_MONTH

        val year = todayCal max MONTH max DAY_OF_MONTH

        val nextYear = todayCal ++ YEAR max MONTH max DAY_OF_MONTH

        if (date before tomorrow.getTime) v.tomorrow()
        else if (date before week.getTime) v.thisWeek()
        else if (date before nextWeek.getTime) v.nextWeek()
        else if (date before month.getTime) v.thisMonth()
        else if (date before nextMonth.getTime) v.nextMonth()
        else if (date before year.getTime) v.thisYear()
        else if (date before nextYear.getTime) v.nextYear()
        else v.future()
      } else {

        import Calendar._

        val yesterday = todayCal -- DATE

        val week = todayCal min DAY_OF_WEEK

        val lastWeek = todayCal -- WEEK_OF_YEAR min DAY_OF_WEEK

        val month = todayCal min DAY_OF_MONTH

        val lastMonth = todayCal -- MONTH min DAY_OF_MONTH

        val year = todayCal min MONTH min DAY_OF_MONTH

        val lastYear = todayCal -- YEAR min MONTH min DAY_OF_MONTH

        if (date after yesterday.getTime) v.yesterday()
        else if (date after week.getTime) v.thisWeek()
        else if (date after lastWeek.getTime) v.lastWeek()
        else if (date after month.getTime) v.thisMonth()
        else if (date after lastMonth.getTime) v.lastMonth()
        else if (date after year.getTime) v.thisYear()
        else if (date after lastYear.getTime) v.lastYear()
        else v.past()
      }
    }

    def humanizedExactDate(implicit usr: AbstractUsr): String =
      if (isToday) "today"
      else if (isYesterday) "yesterday"
      else if (isTomorrow) "tomorrow"
      else if (isThisWeek) new SimpleDateFormat("EEEE").format(date)
      else if (isThisMonth) new SimpleDateFormat("MMM d").format(date)
      else new SimpleDateFormat("d MMM yyyy").format(date)

    def humanizedApproxDate(implicit usr: AbstractUsr): String = {
      // Name collision:
      val _today = dateUtil.today

      accept(new SemanticDateVisitor[String] {

        def today() = "today"

        def yesterday() = "yesterday"

        def tomorrow() = "tomorrow"

        def thisWeek() =
          (if (date before _today) "earlier" else "later") + " " + "this week"

        def lastWeek() = "last week"

        def nextWeek() = "next week"

        def thisMonth() =
          (if (date before _today) "earlier" else "later") + " " + "this month"

        def lastMonth() = "last month"

        def nextMonth() = "next month"

        def thisYear() =
          (if (date before _today) "earlier" else "later") + " " + "this year"

        def lastYear() = "last year"

        def nextYear() = "next year"

        def past() = "years ago"

        def future() = "years from now"
      })
    }
  }

}