package com.github.david04.svapp.bootstrap

import vaadin.scala._
import collection.mutable
import com.github.david04.svapp.base.SVApp

trait BSTablesSVAppComponent {
  svApp: SVApp =>

  object Table {
    val STRIPED = "table-striped"
    val BORDERED = "table-bordered"
    val HOVER = "table-hover"
    val CONDENSED = "table-condensed"
  }

  abstract class Table extends CssLayout {

    def toId(o: AnyRef) = Integer.toString(o.hashCode(), Character.MAX_RADIX)

    val tId = "_t" + toId(this)
    protected val tableStyles = mutable.Set[String]()
    tableStyles += "table"

    private var fhead: () => Seq[(Seq[String], Any)] = () => null
    private var fbody: () => Seq[(Seq[String], Seq[(Seq[String], Any)])] = () => null

    // ==== HEAD setters ====
    def tHead(d: Seq[Any]) { tHead(() => d) }

    def tHead(d: () => Seq[Any]) { tHeadStyled(() => d().map(c => (Seq(), c))) }

    def tHeadStyled(d: Seq[(Seq[String], Any)]) { tHeadStyled(() => d) }

    def tHeadStyled(d: () => Seq[(Seq[String], Any)]) { fhead = d; refresh() }

    // ==== HEAD setters ====

    def tBody(d: Seq[Seq[Any]]) { tBody(() => d) }

    def tBody(d: () => Seq[Seq[Any]]) { tBodyStyled(() => d().map(r => (Seq(), r.map(c => (Seq(), c))))) }

    def tBodyStyled(d: Seq[(Seq[String], Seq[(Seq[String], Any)])]) { tBodyStyled(() => d) }

    def tBodyStyled(d: () => Seq[(Seq[String], Seq[(Seq[String], Any)])]) { fbody = d; refresh() }

    def html(thead: Seq[(Seq[String], Any)], tbody: Seq[(Seq[String], Seq[(Seq[String], Any)])]) = "" +
      "<table class=\"" + tableStyles.mkString(" ") + "\">" +
      (if (thead != null)
        "<thead>" +
          s"<tr>" +
          thead.map(c => s"<th${if (c._1.isEmpty == false) (" class='" + c._1.mkString(" ") + "' ") else ""}>" + process(c._2) + "</th>").mkString +
          "</tr>" +
          "</thead>"
      else "") +
      (if (tbody != null)
        "<tbody>" +
          tbody.map(r => "" +
            s"<tr${if (r._1.isEmpty == false) (" class='" + r._1.mkString(" ") + "' ") else ""}>" +
            r._2.map(c => s"<td${if (c._1.isEmpty == false) (" class='" + c._1.mkString(" ") + "' ") else ""}>" + process(c._2) + "</td>").mkString +
            "</tr>"
          ).mkString +
          "</tbody>"
      else "") +
      "</table>"

    private def process(o: Any): String = o match {
      case Some(v) => process(v)
      case None => ""
      case c: Component => s"<div location='__c${toId(c)}'></div>"
      //      case p: PropRO[_] => process(p.value)
      case o => o.toString
    }

    def refresh(): Unit = {
      val thead = fhead()
      val tbody = fbody()
      val contents = html(thead, tbody)

      removeAllComponents()

      add(new CustomLayout() {
        templateContents = contents

        // TODO: thead

        if (tbody != null)
          tbody.foreach(_._2.foreach(_._2 match {
            case c: Component => add(c, s"__c${toId(c)}")
            case _ =>
          }))
      })
    }
  }

}