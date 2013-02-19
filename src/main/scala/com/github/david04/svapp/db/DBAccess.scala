package com.github.david04.svapp.db

import java.sql.Connection
import java.sql.DriverManager
import com.github.david04.svapp.base.SVApp


trait DBSVAppComponent {
  svApp: SVApp =>

  val db = new DB()

  class DB() {

    Class.forName("org.postgresql.Driver")
    var conn: Option[Connection] = None

    def getConnection(): Connection = {
      conn match {
        case Some(c) => c
        case None => {
          conn = Some(DriverManager.getConnection("jdbc:postgresql://localhost/" + svApp.conf.dbname, svApp.conf.dbuser, svApp.conf.dbpass))
          getConnection()
        }
      }
    }

    val lock = new Object

    def withConnection[A](block: Connection => A): A =
      lock.synchronized {
        val connection = getConnection
        try {
          block(connection)
        } finally {
          //        connection.close()
        }
      }

    def withTransaction[A](block: Connection => A): A = {
      withConnection {
        connection =>
          try {
            connection.setAutoCommit(false)
            val r = block(connection)
            connection.commit()
            r
          } catch {
            case e: Exception => {
              connection.rollback()
              throw e
            }
          }
      }
    }
  }

}