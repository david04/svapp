package com.github.david04.svapp.bootstrap

import vaadin.scala.{Component, Label, Button, CssLayout}
import vaadin.scala.Label.ContentMode
import com.vaadin.server.ClientConnector.{AttachEvent, AttachListener}


abstract class Modal() extends CssLayout {

  private val mId = Integer.toString(this.hashCode(), Character.MAX_RADIX)

  protected lazy val header: String = ""
  protected lazy val btnCaption: String = ""
  protected lazy val btnStyles: Set[String] = Set("btn", BtnClasses.PRIMARY)
  private var fbody: () => Component = null

  protected def body(c: Component) = fbody = () => c

  protected def body(f: () => Component) = fbody = f

  val openBtn =
  //    add(new CssLayout() with InnerHtml {innerHtml = s"<a href='#$mId' role='button' class='${btnStyles.mkString(" ")}' data-toggle='modal'>$btnCaption</a>"})
    new Button() with InnerHtml {
      innerHtml = s"<a href='#$mId' role='button' class='${btnStyles.mkString(" ")}' data-toggle='modal'>$btnCaption</a>"
      clickListeners += (_ => refresh())
    }

  class ModalOpenButton extends Button with Attributes {
    attributes("href") = "#" + mId
    attributes("role") = "button"
    attributes("data-toggle") = "modal"

    p.addAttachListener(new AttachListener {
      def attach(event: AttachEvent) { clickListeners += (_ => refresh()) }
    })
  }

  protected val closeBtn = new Button() with Attributes {
    styleNames += "btn"
    attributes("data-dismiss") = "modal"
    attributes("aria-hidden") = "true"
    caption = "Close"
  }
  protected val saveBtn = new Button() with Attributes {styleNames +=("btn", BtnClasses.PRIMARY); attributes("data-dismiss") = "modal"; caption = "Save"}
  private val modalbody = new Div("modal-body") {}

  add(new Div("modal", "hide", "fade") with Attributes {
    id = mId
    attributes("role") = "dialog"
    attributes("aria-labelledby") = "l" + mId
    attributes("aria-hidden") = "true"
    attributes("tabindex") = "-1"

    add(new Div("modal-header") {
      add(new Div() with InnerHtml {innerHtml = "<button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button>"})
      add(new Label() {value = s"<h3 id='${"l" + mId}'>$header</h3>"; contentMode = ContentMode.Html})
    })
    add(modalbody)
    add(new Div("modal-footer") {add(closeBtn); add(saveBtn)})
  })

  protected def refresh() {
    modalbody.removeAllComponents()
    val _body = fbody()
    modalbody.add(_body)
  }
}