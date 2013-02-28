package com.github.david04.svapp.util

import vaadin.scala._
import com.github.david04.svapp.base.SVApp

trait NaviagationSVAppComponent {
  svApp: SVApp =>

  class HierarchicalURLItem(val itemNameRegex: String, private val pItem: HierarchicalURLItem, authRequired: Boolean)(createItem: (HierarchicalURLItem, String, AbstractLayout) => Component) extends CssLayout with Logging {
    width = Measure(100, Units.pct)

    def cntx: List[String] = pItem.cntx ::: List(openInstance.getOrElse(itemNameRegex))

    val children = collection.mutable.Map[HierarchicalURLItem, Boolean]()
    var openChild: Option[HierarchicalURLItem] = None
    var openInstance: Option[String] = None
    var openComponent: Option[Component] = None
    val thisLayout = this

    protected implicit val thisHUI = this

    protected def close() {
      if (openInstance.isDefined) {
        children.foreach(_._1.close())
        children.clear()
        removeComponent(openComponent.get)
        openInstance = None
        openComponent = None
      }
    }

    def addChild(child: HierarchicalURLItem): HierarchicalURLItem = {
      children(child) = false
      child
    }

    def open(matched: String, context: List[String]) {
      if (authRequired && !svApp.isLoggedIn())
        throw new RedirectException(cntxt.LOGIN(Some((Iterator.iterate[HierarchicalURLItem](pItem)(_.pItem).takeWhile(_ != null).drop(1).map(_.openInstance.get).toSeq ++ Seq(matched) ++ context).mkString("/"))))
      else {
        openInstance match {
          case Some(instance) if instance == matched => // Nothing to do: opened and same match
          case Some(instance) => {close(); openComponent = Some(add(createItem(this, matched, thisLayout)))} // Opened but with different params
          case None => {openComponent = Some(add(createItem(this, matched, thisLayout)))} // Closed - have to open
        }
        openInstance = Some(matched)

        val matches = if (!context.isEmpty) children.keySet.filter(kv => context.head.matches(kv.itemNameRegex)) else collection.mutable.Set[HierarchicalURLItem]()
        children.keySet.diff(matches).foreach(_.close())
        matches.foreach(_.open(context.head, context.tail))
        if (!context.isEmpty && matches.isEmpty) {
          log.error("WARN: undefined handler for '" + context.mkString("/") + "'. Loading default page..")
          throw new RedirectException(svApp.defaultCntxt())
        }
      }
    }

    override def detach() = {
      close()
      super.detach()
    }
  }

  class RootHierarchicalURLItem(createItem: (HierarchicalURLItem, String, AbstractLayout) => Component) extends HierarchicalURLItem("", null, false)(createItem) {

    def open(context: List[String]) {
      try {super.open("", context)}
      catch {
        case RedirectException(to) => svApp.openContext(to)
      }
    }

    override def cntx: List[String] = List()
  }

  object HierarchicalURLItem {

    def apply(itemName: String, parent: HierarchicalURLItem, authRequired: Boolean = false)(createItem: (HierarchicalURLItem, String) => Component) = new HierarchicalURLItem(itemName, parent, authRequired)((i, s, _) => createItem(i, s))

    def withLayout(itemName: String, parent: HierarchicalURLItem, authRequired: Boolean = false)(createItem: (HierarchicalURLItem, String, AbstractLayout) => Component) = new HierarchicalURLItem(itemName, parent, authRequired)(createItem)
  }

  case class RedirectException(cntxt: List[String]) extends Exception {}

}