package com.github.david04.svapp.base

import vaadin.scala._

import com.vaadin.server._
import com.github.david04.svapp.util._
import com.github.david04.svapp.model.{DBUsrSVAppComponent, DBAccountSVAppComponent}
import org.apache.log4j.Logger
import com.vaadin.server
import server.Page.UriFragmentChangedEvent
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}
import com.github.david04.svapp.db.{DBClassSVAppComponent, DBSVAppComponent, DBPropSVAppComponent}
import com.github.david04.db.DBCompanionSVAppComponent
import com.github.david04.svapp.view.{ErrorsHelperSVAppComponent, CntxtSVAppComponent}
import com.github.david04.svapp.mail.MailServiceSVAppComponent
import com.github.david04.svapp.bootstrap.{InnerHtml, BSTablesSVAppComponent}
import javax.servlet.http
import com.github.david04.svapp.bootstrap

case class ContextChangeEvent(cntxt: Seq[String])

trait ContextChangeListener[E] {
  def contextChanged(e: E): Unit
}

trait SVAppDB extends DBSVAppComponent
with DBPropSVAppComponent
with DBCompanionSVAppComponent
with DBClassSVAppComponent
with DBAccountSVAppComponent
with DBUsrSVAppComponent
with ConfSVAppComponent
with MailServiceSVAppComponent
with DateUtilsSVAppComponent
with Logging {

  implicit def usr: AbstractUsr
}

trait SVApp extends UI
with SVAppDB
with NaviagationSVAppComponent
with CntxtSVAppComponent
with ErrorsHelperSVAppComponent
with LayoutUtilsSVAppComponent
with BSTablesSVAppComponent {

  implicit val svApp: this.type = this

  lazy val appCache = collection.mutable.Map[Any, Any]()


  def request: HttpServletRequest = VaadinService.getCurrentRequest().asInstanceOf[VaadinServletRequest].getHttpServletRequest

  def response: HttpServletResponse = VaadinService.getCurrentResponse().asInstanceOf[VaadinServletResponse].getHttpServletResponse

  def isLoggedIn() = usr != null

  val contextChangeListeners = collection.mutable.Set[ContextChangeListener[ContextChangeEvent]]()

  protected def root: RootHierarchicalURLItem

  protected def dologin(user: AbstractUsr, cntx: Option[List[String]] = None) {
    Logger.getLogger(this.getClass).info(s"Login: Id(${user.id}) Name(${user.name}) Account(${user.account.v.id}})")

    if (conf.dev) bootstrap.Cookie("username") = user.email.value

    cntx match {
      case Some(c) => page.setUriFragment(c.mkString("/"), true)
      case None => {
      }
    }
  }

  def logOut() {
    if (conf.dev) bootstrap.Cookie("username") = ""
    openContext(cntxt.LOGIN)
  }

  def openExternalURL(url: String) {
    page.open(url, "_blank")
  }

  def openURL(url: String) {
    page.open(url, null)
  }

  private[this] var last: Option[List[String]] = None
  private[this] var current: Option[List[String]] = None

  def back(): Unit = openContext(last.getOrElse(defaultCntxt()))

  def openContext(context: List[String]) {
    DebugTime("Open") {
      page.setUriFragment(context.mkString("/"), true)
    }
  }

  def currentContext() = current match {
    case Some(c) => c
    case None => List[String]()
  }

  override def init(req: ScaladinRequest): Unit = DebugTime("Init") {
    Logger.getLogger(this.getClass).info("IP: " + request.getRemoteAddr)
    Logger.getLogger(this.getClass).trace("Headers: " + {
      val h = request.getHeaderNames().asInstanceOf[java.util.Enumeration[java.lang.String]]
      Iterator.continually((h, h.nextElement)).takeWhile(_._1.hasMoreElements).map(_._2)
        .map(name => name + " => '" + request.getHeader(name) + "'")
        .mkString("\n\t", "\n\t", "\n")
    })

    // Content
    val r = root
    content = r

    // Automatic login
    tryAutoLogin()

    //    page.p.addURIHandler(new URIHandler {
    //      def handleURI(context: URL, relativeUri: String): DownloadStream = {
    //        null
    //      }
    //    })

    page.p.addUriFragmentChangedListener(new server.Page.UriFragmentChangedListener {

      def uriFragmentChanged(event: UriFragmentChangedEvent) {
        if (isLoggedIn()) Logger.getLogger(this.getClass).info("User '" + usr.email.value + "' requested page: " + page.uriFragment.getOrElse("<none>"))
        else Logger.getLogger(this.getClass).info("Visitor " + request.getRemoteAddr + " (" + request.getLocale + ")" + " requested page: " + page.uriFragment.getOrElse("<none>"))

        if (page.uriFragment.getOrElse("") != "") {
          try {
            DebugTime("Open " + page.uriFragment.get) {
              if (current != null) last = current
              val context = page.uriFragment.get.split('/').toList
              r.open(context)
              current = Some(context)
              contextChangeListeners.foreach(_.contextChanged(ContextChangeEvent(context)))
            }
          } catch {
            case e: SVAppException => openContext(cntxt.ERROR(e.errorCode))
          }
        }
      }
    })

    page.uriFragment match {
      case None => openContext(defaultCntxt())
      case Some("") => openContext(defaultCntxt())
      case Some(frag) => {page.setUriFragment("", false); page.setUriFragment(frag, true)}
    }
  }

  protected def tryLogin(email: String): Boolean

  private def tryAutoLogin() {
    val lastUser =
      if (request.getCookies == null) ""
      else request.getCookies.find(_.getName == "username") match {
        case Some(c) => c.getValue
        case None => ""
      }

    if (conf.dev && lastUser != "") {
      log.info("Auto login " + lastUser)

      if (!tryLogin(lastUser)) {
        log.warn("Unknown user: " + lastUser)
        bootstrap.Cookie("username") = ""
      }
    }
  }


  def defaultCntxt(): List[String]

  class SVAppException(val errorCode: Int) extends Exception {}

}
