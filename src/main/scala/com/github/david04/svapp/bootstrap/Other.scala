package com.github.david04.svapp.bootstrap

import vaadin.scala.{Component, AbstractTextField}
import java.util.UUID
import com.vaadin.server.ClientConnector.{AttachEvent, AttachListener}
import org.json.simple.JSONValue

trait Placeholder {
  self: AbstractTextField =>

  private var _prompt: Option[String] = None

  p.addAttachListener(new AttachListener {
    def attach(event: AttachEvent): Unit =
      _prompt match {
        case None =>
        case Some(p) => {
          if (!self.id.isDefined) id = UUID.randomUUID().toString.take(6)
          ui.get.page.p.getJavaScript.execute("document.getElementById(\"" + id.get + "\").setAttribute(\"placeholder\", \"" + p + "\")")
        }
      }
  })

  override def prompt: Option[String] = _prompt

  override def prompt_=(p: Option[String]) { _prompt = p }

  override def prompt_=(prompt: String) { prompt_=(Some(prompt)) }
}

trait Attributes {
  self: Component =>

  val attributes = collection.mutable.Map[String, String]()

  p.addAttachListener(new AttachListener {
    def attach(event: AttachEvent): Unit =
      if (attributes.isEmpty == false) {
        if (!self.id.isDefined) id = UUID.randomUUID().toString.take(6)
        ui.get.page.p.getJavaScript.execute(
          attributes.map(att => s"document.getElementById('${id.get}').setAttribute('${att._1}', '${att._2}');").mkString("\n"))
      }
  })
}

trait InnerHtml {
  self: Component =>

  private val t = this
  private var _innerHtml: Option[String] = None
  private val childreen = collection.mutable.Map[String, String]()

  private def update() {
    if ((_innerHtml.isDefined || childreen.size > 0) && ui.isDefined) {
      val jscode =
        (if (_innerHtml.isDefined) "document.getElementById(\"" + id.get + "\").innerHTML = \"" + JSONValue.escape(_innerHtml.get) + "\"; \n" else "") +
          childreen.map(c => "document.getElementById(\"" + c._1 + "\").innerHTML = \"" + JSONValue.escape(c._2) + "\";").mkString(" \n")
      ui.get.page.p.getJavaScript.execute(jscode)
    }
  }

  def innerHtml_=(html: String): Unit = { _innerHtml = Some(html); update() }

  def innerHtml: Option[String] = _innerHtml

  def childInnerHtml = new Object {
    def update(id: String, html: String) { childreen(id) = html }
  }

  p.addAttachListener(new AttachListener {
    def attach(event: AttachEvent): Unit = {
      if (!self.id.isDefined) id = Integer.toString(t.hashCode(), Character.MAX_RADIX)
      update()
    }
  })
}

case class Validator(showError: Option[String] => Unit, fields: (AbstractTextField, String, (String) => Boolean, String)*) {

  var valid = false

  private def validate() {
    fields.foreach(_ match {
      case (field, empty, validator, invalid) => {
        field.value match {
          case Some("") => valid = false
          case Some(value: String) => if (!validator(value)) {valid = false; showError(Some(invalid))}
          case _ => valid = false
        }
      }
    })
  }

  fields.foreach(_ match {
    case (field, empty, validator, error) => {
      field.immediate = true
      field.valueChangeListeners += (_ => {
        field.value match {
          case None => {valid = false; showError(Some(empty))}
          case Some("") => {valid = false; showError(Some(empty))}
          case _ => {showError(None); valid = true; validate()}
        }
      })
    }
  })

}
