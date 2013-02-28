package com.github.david04.svapp.bootstrap

import vaadin.scala.{Label, CustomLayout, Button}


object BtnClasses {
  type BtnClass = String
  val DEFAULT: BtnClass = ""
  val PRIMARY: BtnClass = "btn-primary"
  val INFO: BtnClass = "btn-info"
  val SUCCESS: BtnClass = "btn-success"
  val WARNING: BtnClass = "btn-warning"
  val DANGER: BtnClass = "btn-danger"
  val INVERSE: BtnClass = "btn-inverse"
  val LINK: BtnClass = "btn-link"

  val all = Set(PRIMARY, INFO, SUCCESS, WARNING, DANGER, INVERSE, LINK)
}

object BtnSizes {
  type BtnSize = String
  val LARGE: BtnSize = "btn-large"
  val DEFAULT: BtnSize = ""
  val SMALL: BtnSize = "btn-small"
  val MINI: BtnSize = "btn-mini"
}

class BtnGroup(name: String, btns: Seq[Button], btnClass: BtnClasses.BtnClass = BtnClasses.DEFAULT, addtionalStyles: String = "") extends CustomLayout() {
  width = None
  styleNames += "display-inline-block"
  val btnStyleNames = collection.mutable.Set("btn", btnClass, "dropdown-toggle")
  templateContents =
    "<div class=\"btn-group " + addtionalStyles + "\">" +
      "  <button class=\"" + btnStyleNames.mkString(" ") + "\" data-toggle=\"dropdown\">" + name + " <span class=\"caret\"></span></button>" +
      "  <ul class=\"dropdown-menu\">" +
      (0 to (btns.size - 1)).map(i => "<div location=\"__btn" + i + "\"></div>").mkString +
      "  </ul>" +
      "</div> &nbsp; "

  btns.zipWithIndex.foreach(b => add(b._1, "__btn" + b._2))
}

class ComboBtnGroup(selected: Button, btns: Seq[Button], btnClass: BtnClasses.BtnClass = BtnClasses.DEFAULT, addtionalStyles: String = "") extends CustomLayout() {
  width = None
  styleNames += "display-inline-block"
  val btnStyleNames = collection.mutable.Set("btn", btnClass, "dropdown-toggle")
  templateContents =
    "<div class=\"btn-group " + addtionalStyles + "\">" +
      "  <button class=\"" + btnStyleNames.mkString(" ") + "\" data-toggle=\"dropdown\">" + """<div class="display-inline-block" location="__lbl"></div> """ + " <span class=\"caret\"></span></button>" +
      "  <ul class=\"dropdown-menu\">" +
      (0 to (btns.size - 1)).map(i => "<div location=\"__btn" + i + "\"></div>").mkString +
      "  </ul>" +
      "</div> &nbsp; "

  val lbl = new Label() {width = None; value = selected.caption.get}
  add(lbl, "__lbl")

  def selected_=(b: Button) { lbl.value = b.caption.get }

  btns.foreach(b => b.clickListeners += (_ => selected_=(b)))

  btns.zipWithIndex.foreach(b => add(b._1, "__btn" + b._2))
}
