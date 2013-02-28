package com.github.david04.svapp.bootstrap

import vaadin.scala.{Label, CssLayout}
import vaadin.scala.Label.ContentMode

class Span1() extends CssLayout {styleNames += "span1"}

class Span2() extends CssLayout {styleNames += "span2"}

class Span3() extends CssLayout {styleNames += "span3"}

class Span4() extends CssLayout {styleNames += "span4"}

class Span5() extends CssLayout {styleNames += "span5"}

class Span6() extends CssLayout {styleNames += "span6"}

class Span7() extends CssLayout {styleNames += "span7"}

class Span8() extends CssLayout {styleNames += "span8"}

class Span9() extends CssLayout {styleNames += "span9"}

class Span10() extends CssLayout {styleNames += "span10"}

class Span11() extends CssLayout {styleNames += "span11"}

class Span12() extends CssLayout {styleNames += "span12"}


class BoxWidget() extends CssLayout {styleNames += "box-widget"}

class WidgetHead(private var wtitle: String = "", private var wicon: String = "") extends CssLayout {
  styleNames +=("widget-head", "clearfix")
  private val lbl = new Label() {width = None; contentMode = ContentMode.Html}
  add(lbl)

  private def update(): Unit = lbl.value = s"<span class='h-icon'><i class='gray-icons $wicon '></i></span><h4> $wtitle </h4>"

  update()

  def title_=(t: String): Unit = { wtitle = t; update() }

  def icon_=(i: String): Unit = { wicon = i; update() }
}

class WidgetContainer() extends CssLayout {styleNames += "widget-container"}

class WidgetBlock() extends CssLayout {styleNames +=("widget-block", "clearfix")}

class FormContainer() extends CssLayout {styleNames += "form-container"}

class ErrorContainer() extends CssLayout {styleNames += "error-container"}

class ControlGroup extends Div("control-group")

class Controls extends Div("controls")

class Div extends CssLayout {

  protected implicit val div = this

  def this(s1: String) { this(); styleNames += (s1) }

  def this(s1: String, s2: String) { this(); styleNames +=(s1, s2) }

  def this(s1: String, s2: String, s3: String) { this(); styleNames +=(s1, s2, s3) }

  def this(s1: String, s2: String, s3: String, s4: String) { this(); styleNames +=(s1, s2, s3, s4) }
}

class ContainerFluid() extends Div("container-fluid")

class RowFluid() extends Div("row-fluid")

class Block() extends Div("block") {}

class BlockHeading() extends Div("block-heading")

class BlockBody() extends Div("block-body")

class BtnToolbar() extends Div("btn-toolbar")
