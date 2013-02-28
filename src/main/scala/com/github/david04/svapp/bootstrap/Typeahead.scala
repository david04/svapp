package com.github.david04.svapp.bootstrap

import vaadin.scala._

trait Typeahead extends Component with Attributes {
  def values: Seq[String]
  val dataItems: Int = 4
  attributes("data-source") = values.mkString("[\"", "\",\"", "\"]")
  attributes("data-provide") = "typeahead"
  attributes("data-items") = "" + dataItems
}