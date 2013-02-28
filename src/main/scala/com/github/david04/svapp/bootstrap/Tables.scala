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
    protected val tableStyles = mutable.Set[String]("table")

    private var fhead: () => Seq[(Seq[String], Any)] = () => null
    private var fbody: () => Seq[(Seq[String], Seq[(Seq[String], Any)])] = () => null

    protected val paginate: Boolean = false
    protected val pageSize: Int = 12
    protected val maxPagesShowing: Int = 5
    protected var currentPage: Int = 1

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

    def pagination(current: Int, npages: Int, btns: List[Int]): String = {
      val prevStyle = if (current == 1) "disabled" else ""
      val nextStyle = if (current == npages) "disabled" else ""

      "" +
        "<div style=\"text-align:center;\">" +
        "<div class=\"pagination\">" +
        "<ul>" +
        s"<li class='$prevStyle'><a><div location='__page_prev'></div></a></li>" +
        btns.map(i => s"<li class='${if (i == current) "active" else ""}'><a><div location='__page_$i'></div></a></li>").mkString +
        s"<li class='$nextStyle'><a><div location='__page_next'></div></a></li>" +
        "</ul>" +
        "</div>" +
        "</div>"
    }

    private def process(o: Any): String = o match {
      case Some(v) => process(v)
      case None => ""
      case c: Component => s"<div location='__c${toId(c)}'></div>"
      //      case p: PropRO[_] => process(p.value)
      case o => o.toString
    }

    def refresh(): Unit = {
      val thead = fhead()
      val all = fbody()
      if (all != null) {
        val npages = math.ceil(all.size.toDouble / pageSize).toInt
        currentPage = math.min(npages, currentPage)
        val tbody = if (!paginate || all.isEmpty) all else all.grouped(pageSize).toSeq(currentPage - 1)
        val contents =
          if (!paginate || all.isEmpty) html(thead, tbody)
          else {
            val stdSize = (maxPagesShowing - 1) / 2

            val leftAvailable = currentPage - 1
            val rightAvailable = npages - currentPage

            val nLeft = math.min(leftAvailable, stdSize) + math.max(stdSize - rightAvailable, 0)
            val nRight = math.min(rightAvailable, stdSize) + math.max(stdSize - leftAvailable, 0)

            val btns = ((currentPage - nLeft) to (currentPage + nRight)).toList

            html(thead, tbody) + pagination(currentPage, npages, btns)
          }

        removeAllComponents()

        add(new CustomLayout() {
          templateContents = contents

          // TODO: thead

          tbody.foreach(_._2.foreach(_._2 match {
            case c: Component => add(c, s"__c${toId(c)}")
            case _ =>
          }))

          if (paginate && !all.isEmpty) {
            add(new Button() {
              styleNames +=("padding-right-12px", "padding-left-12px")
              caption = "Prev"
              clickListeners += (_ => {currentPage = math.max(1, currentPage - 1); refresh()})
            }, "__page_prev")
            (1 to npages).foreach(i =>
              add(new Button() {
                styleNames +=("padding-right-12px", "padding-left-12px")
                caption = "" + i
                clickListeners += (_ => {currentPage = i; refresh()})
              }, "__page_" + i))
            add(new Button() {
              styleNames +=("padding-right-12px", "padding-left-12px")
              caption = "Next"
              clickListeners += (_ => {currentPage = math.min(npages, currentPage + 1); refresh()})
            }, "__page_next")
          }
        })
      }
    }
  }

}