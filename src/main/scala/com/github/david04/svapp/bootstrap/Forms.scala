package com.github.david04.svapp.bootstrap

import vaadin.scala._
import scala.Some
import collection.mutable.ListBuffer


object ValidationState {
  val NORMAL = ""
  val WARN = "warning"
  val ERROR = "error"
  val INFO = "info"
  val SUCCESS = "success"
}

trait FormField {
  private[bootstrap] var form: Form

  var valid: Boolean

  val layout: Layout
}

abstract class FormTextField(val name: String) extends TextField with FormField {

  private[bootstrap] var form: Form = null.asInstanceOf[Form]
  protected val tf = this
  private val errorLabel = new Label() {
    styleName = ValidationState.ERROR
    value = ""
  }
  val valdtrs = ListBuffer[(String => Boolean, String)]()
  var valid: Boolean = true

  valueChangeListeners += (_ => revalidate())
  immediate = true

  def require(test: String => Boolean, error: String): Unit = valdtrs += ((test, error))

  def revalidate() {
    valdtrs.find(_._1(value.getOrElse("").toString) == false) match {
      case Some(v) => {valid = false; errorLabel.value = v._2; form.update()}
      case None => {valid = true; errorLabel.value = ""; form.update()}
    }
  }

  val layout = new CustomLayout() {
    templateContents =
      "<div class=\"control-group\">" +
        s"<label class='control-label'>$name</label>" +
        "<div location='__controls'></div>" +
        "</div>"

    add(new Controls() {
      add(tf)
      add(errorLabel)
    }, "__controls")
  }
}

abstract class Form(formStyle: String = "form-horizontal") extends CustomLayout {

  private val form = this
  private var hasErrors: Boolean = false

  val fields: Seq[FormField]

  private[bootstrap] def update() {
    hasErrors = fields.exists(_.valid == false)
    if (hasErrors) submitBtn.styleNames -= "btn-primary"
    else submitBtn.styleNames += "btn-primary"
  }

  templateContents = s"<form class='$formStyle'><div location='__contents'></div></form>"

  val externalBtns = false
  val submitBtn = new Button() {styleNames +=("btn", "btn-primary"); caption = "Save"}
  val cancelBtn = new Button() {styleNames +=("btn", "margin-left-10"); caption = "Cancel"}

  //TODO: Call this in subclass
  def createForm() {
    submitBtn.clickListeners += (_ => if (!hasErrors) action())
    cancelBtn.clickListeners += (_ => cancel())
    add(new CssLayout() {
      fields.foreach(field => {
        field.form = form
        add(field.layout)
      })

      if (!externalBtns)
        add(new Div("form-actions") {
          add(submitBtn)
          add(cancelBtn)
        })
    }, "__contents")
  }

  def action(): Unit

  def cancel(): Unit
}
