package com.github.david04.db

import anorm._
import java.sql.Connection
import com.github.david04.svapp.base.SVApp

object DBCache {
  val cache = collection.mutable.Map[String, collection.mutable.Map[Int, _]]()
}

trait DBCompanionSVAppComponent {
  svApp: SVApp =>

  trait DBCompanionObjectTrait[T <: DBObjectClassTrait] {

    val createdListeners = collection.mutable.Set[(T, AbstractUsr) => Unit]()
    val deletedListeners = collection.mutable.Set[(T, AbstractUsr) => Unit]()

    // Must be a def
    def table: String

    protected val orderBy: Option[String] = None

    protected def orderBySql = (orderBy match {
      case Some(field) => " ORDER BY " + field
      case None => " "
    })

    protected def parse(stream: Stream[SqlRow]): T

    val cache = DBCache.cache.getOrElse(table, collection.mutable.Map[Int, T]()).asInstanceOf[collection.mutable.Map[Int, T]]

    def fromId(id: Int): T = {
      val v = db withConnection (implicit connection =>
        cache.getOrElseUpdate(id, {
          val rows = SQL("select * from " + table + " where id={id}").onParams(id)()
          if (rows.isEmpty) throw new Exception("Asked for " + table + " with id=" + id + " but it does not exit!")
          parse(rows)
        }))
      //    if (usr != null) {
      //      val c: Class[_] = v.getClass()
      //      c.getDeclaredFields.find(_.getName == "account") match {
      //        case Some(f) => {
      //          f.setAccessible(true)
      //          if (f.get(v) != null && !(f.get(v).toString.toInt == usr.accountid))
      //            throw new SVAppException(ErrorsHelper.ERROR_NO_PERMISSIONS)
      //        }
      //        case _ =>
      //      }
      //    }
      v
    }

    protected def fromIds(ids: Seq[Int]): Seq[T] = ids.map(fromId(_))

    protected def maxId()(implicit c: Connection): T = fromId(SQL("select max(id) from " + table)().head.data.head.toString.toInt)

    def delete(id: Int): Unit =
      this.synchronized({
        val o = fromId(id)
        cache.remove(id)
        db withConnection (implicit c => SQL("delete from " + table + " where id={id}").onParams(id).executeUpdate())
        deletedListeners.foreach(_(o, usr))
      })

    private def whereAccountIs: String = usr match {
      case null => ""
      case _ => " where accountid=" + usr.account.value.id + " "
    }

    private def andAccountIs: String = usr match {
      case null => ""
      case _ => " and accountid=" + usr.account.value.id + " "
    }

    def allAdmin(): Seq[T] =
      db withConnection (implicit connection =>
        fromIds(SQL("select id from " + table + orderBySql)().collect {
          case Row(id: Int) => id
        }))

    def all(): Seq[T] =
      db withConnection (implicit connection =>
        fromIds(SQL("select id from " + table + whereAccountIs + orderBySql)().collect {
          case Row(id: Int) => id
        }))

    protected def where[Q](name: String, value: Q, condition: String = "="): Seq[T] =
      db withConnection (implicit connection =>
        fromIds(SQL("select id from " + table + " where " + name + " " + condition + "{" + name + "}" + andAccountIs + orderBySql).onParams(value)().collect {
          case Row(id: Int) => id
        }))

    protected def where(fields: (String, Any)*): Seq[T] =
      db withConnection (implicit connection =>
        fromIds(SQL("select id from " + table + " where " + fields.map(f => f._1 + " = " + f._2).mkString(" and ") + andAccountIs + orderBySql).
          on(fields.map(f => (f._1, anorm.toParameterValue(f._2))): _*)().collect {
          case Row(id: Int) => id
        }))

    protected def whereCmplx(fields: (String, Any, String)*): Seq[T] =
      db withConnection (implicit connection => {
        fromIds(SQL("select id from " + table + " where " + fields.map(f => f._1 + " " + f._3 + " " + f._2).mkString(" and ") + andAccountIs + orderBySql).
          on(fields.map(f => (f._1, anorm.toParameterValue(f._2))): _*)().collect {
          case Row(id: Int) => id
        })
      })

    private def includeAccount(fields: List[(String, Any)]) = usr match {
      case null => fields
      case _ => fields.toMap.get("accountid") match {
        case Some(id) => fields
        case None => ("accountid", usr.account.value.id) :: fields
      }
    }

    protected def createRow(_fields: (String, Any)*): T = {
      val fields = includeAccount(_fields.toList)
      this.synchronized {
        db withConnection (implicit connection => {

          fields match {
            case Nil =>
              SQL("insert into " + table + " DEFAULT VALUES").executeUpdate()
            case _ =>
              SQL("insert into " + table + "(" + fields.map(_._1).mkString(",") + ") VALUES (" + fields.map("{" + _._1 + "}").mkString(",") + ")").
                on(fields.map(f => (f._1, anorm.toParameterValue(f._2))): _*).executeUpdate()
          }

          val created = maxId()

          createdListeners.foreach(_(created, usr))

          created
        })
      }
    }
  }

}