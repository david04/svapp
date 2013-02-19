package com.github.david04.svapp.view

import vaadin.scala._
import vaadin.scala.Measure
import com.github.david04.svapp.base.SVApp


trait ErrorsHelperSVAppComponent {
  svApp: SVApp =>

  val errorsHelper = new ErrorsHelper()

  class ErrorsHelper {

    def ERROR_CNTX(code: Int) = List("error-" + code)

    def ERROR_CNTX() = List("error-\\d+")

    val ERROR_NO_PERMISSIONS = 1
    val ERROR_INVALID_ARGUMENTS = 2

    private def messages(implicit usr: AbstractUsr) = Map[Int, String](
      (ERROR_NO_PERMISSIONS, "No Permissions"),
      (ERROR_INVALID_ARGUMENTS, "Internal Error")
    )

    def errorsLayout(parent: HierarchicalURLItem): HierarchicalURLItem =
      HierarchicalURLItem(ERROR_CNTX().last, parent)((thisItem, matched) =>
        new VerticalLayout() {

          val errorCode = matched.substring(6).toInt

          add(new VerticalLayout() {
            {
              width = Measure(100, Units.pct)
              height = Measure(400, Units.px)
              width = Measure.apply(100, Units.pct)
              add(new Label() {
                width = None
                val msg = messages(usr).getOrElse(errorCode, "An unknown error occured")
                value = msg
                styleName = "h3"
              }, alignment = Alignment.MiddleCenter)
            }
          })
        })
  }

}