package com.github.david04.svapp.util

import com.vaadin.server._
import vaadin.scala.ScaladinServlet
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

abstract class AbstractSVAppServlet extends ScaladinServlet {

  val js: List[String]

  override protected def servletInitialized {
    getService.addSessionInitListener(new SessionInitListener {
      def sessionInit(event: SessionInitEvent) {
        event.getSession.addBootstrapListener(new BootstrapListener {
          def modifyBootstrapFragment(response: BootstrapFragmentResponse) {
          }

          def modifyBootstrapPage(response: BootstrapPageResponse) {
            js.foreach(js => response.getDocument.head().appendChild(new Element(Tag.valueOf("script"), "").attr("type", "text/javascript").attr("src", js)))
          }
        })
      }
    })
  }
}