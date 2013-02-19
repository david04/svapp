package com.github.david04.svapp.util.misc

import vaadin.scala.Notification

object PopupMessages {

  def maintenaceMessage: Notification = {
    val msg = Notification("In Maintenance", Notification.Type.Tray)
    msg.description = ""
    msg.delayMsec = 20000 //20sec
    msg.position = Notification.Position.MiddleCenter
    msg
  }
}
