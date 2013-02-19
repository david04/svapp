package com.github.david04.svapp.model

import anorm.SqlRow
import com.github.david04.svapp.base.SVApp

trait DBAccountSVAppComponent {
  svApp: SVApp =>

  abstract class AbstractAccount(implicit row: SqlRow) extends DBObjectClassTrait {

    // Properties
    val id = immutableVal[Int]("id")
  }

  abstract class AbstractAccounts[T <: AbstractAccount] extends DBCompanionObjectTrait[T] {

    lazy val table = "account"
  }

}