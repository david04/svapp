package com.github.david04.svapp.bootstrap

import vaadin.scala._

class Typeahead(values: Seq[String], dataItems: Int = 4) extends TextField() with Attributes {
  attributes("data-source") = values.mkString("[\"", "\",\"", "\"]")
  attributes("data-provide") = "typeahead"
  attributes("data-items") = "" + dataItems
}