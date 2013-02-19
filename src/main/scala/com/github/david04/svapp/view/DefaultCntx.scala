package com.github.david04.svapp.view

import java.net.URLEncoder
import com.github.david04.svapp.base.SVApp


trait CntxtSVAppComponent {
  self: SVApp =>

  val cntxt = new DefaultCntxt()

  class DefaultCntxt {

    def cntxToLink(cntx: List[String]) = {
      "http://" + conf.appDomain + "#" + cntx.mkString("/")
    }

    val LOGIN = List("login(:([^/])+)?")

    def LOGIN(redirect: Option[String]) = List(redirect match {
      case Some(to) => ("login:" + URLEncoder.encode(to))
      case None => "login"
    })

    def ERROR(code: Int) = List("error-" + code)

    def ERROR() = List("error-\\d+")

    val APPLICATION = List("app")
  }

}