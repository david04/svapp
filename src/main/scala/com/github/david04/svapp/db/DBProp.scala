package com.github.david04.svapp.db

import vaadin.scala.AbstractLayout
import com.vaadin.server.ClientConnector.{DetachEvent, DetachListener, AttachEvent, AttachListener}
import com.github.david04.svapp.base.SVApp

trait DBPropSVAppComponent {
  svApp: SVApp =>

  object DBPropertyListenersCounter {

    var nListeners = 0

    def ++() = this.synchronized {
      nListeners = nListeners + 1
    }

    def -=(n: Int) = this.synchronized {
      nListeners -= n
    }
  }

  object PropRO {

    type Listener[T] = (T, T, AbstractUsr) => Unit

  }

  trait PropRO[T] {
    def value: T

    def addValueChangedListener(l: T => Unit, init: Boolean = true)(implicit layout: AbstractLayout): Unit

    private[db] def addValueChangedListenerNoLayout(l: PropRO.Listener[T], init: Boolean = true): Unit

    private[db] def removeValueChangedListenerNoLayout(l: PropRO.Listener[T]): Unit
  }

  trait Prop[T] extends PropRO[T] {

    def value_=(_value: T)(implicit usr: AbstractUsr): Unit

    override def equals(obj: Any): Boolean = throw new Exception("BUG: Testing for Prop equality.")

    override def toString: String = throw new Exception("BUG: Asking for toString() in a property.")
  }

  abstract class PropROImpl[T](val initialValueOpt: Option[T] = None, val pType: DBPropertyType = DBPropertyTypes.UNDEFINED) extends PropRO[T] {

    def this(t: DBPropertyType) = this(None, t)

    protected var _v: T = null.asInstanceOf[T]

    protected var initialized = false

    protected def initialValue() =
      initialValueOpt match {
        case Some(v) => v
        case None => throw new RuntimeException("No initial value provided for property!")
      }

    protected case class ValueChangedListener(handler: PropRO.Listener[T], originLayout: AbstractLayout)

    protected val valueChangeListeners = collection.mutable.Map[PropRO.Listener[T], ValueChangedListener]()

    def value: T = {
      if (!initialized) {
        _v = initialValue()
        initialized = true
      }
      _v
    }

    protected def firePropChanged(old: T)(implicit usr: AbstractUsr) {
      val current = value
      valueChangeListeners.values.foreach(_.handler(old, current, usr))
    }

    protected def value_=(_value: T)(implicit usr: AbstractUsr): Unit = {
      val old = value
      _v = _value
      initialized = true
      firePropChanged(old)
    }

    private def addValueChangedListenerWithLayout(l: (T, AbstractUsr) => Unit, init: Boolean)(implicit layout: AbstractLayout): Unit = {
      if (init) l(value, usr)

      val listener = new AttachListener {
        def attach(event: AttachEvent) {
          val ltnr = (vold: T, v: T, u: AbstractUsr) => l(v, u)
          valueChangeListeners(ltnr) = ValueChangedListener(ltnr, layout)
          DBPropertyListenersCounter ++
        }
      }

      if(layout.p.getParent != null) listener.attach(null)
      layout.p.addAttachListener(listener)
      layout.p.addDetachListener(new DetachListener {
        def detach(event: DetachEvent) {
          val matched = valueChangeListeners.filter(kv => l == kv._2.originLayout).map(_._1)
          valueChangeListeners --= matched
          DBPropertyListenersCounter -= (matched.size)
        }
      })
    }

    private[db] def addValueChangedListenerNoLayout(l: PropRO.Listener[T], init: Boolean = true): Unit = {
      if (init) l(null.asInstanceOf[T], value, null)
      valueChangeListeners(l) = ValueChangedListener(l, null)
      DBPropertyListenersCounter ++
    }

    private[db] def removeValueChangedListenerNoLayout(l: PropRO.Listener[T]) {
      valueChangeListeners -= l
      DBPropertyListenersCounter -= 1
    }

    def addValueChangedListener(l: T => Unit, init: Boolean = true)(implicit layout: AbstractLayout): Unit = addValueChangedListenerWithLayout((v, _) => l(v), init)
  }

  abstract class DBPropRO[T](initialValueOpt: Option[T] = None, pType: DBPropertyType = DBPropertyTypes.UNDEFINED) extends PropROImpl[T](initialValueOpt, pType) {}

  abstract class PropImpl[T](initialValueOpt: Option[T] = None, pType: DBPropertyType = DBPropertyTypes.UNDEFINED) extends PropROImpl[T](initialValueOpt, pType) with Prop[T] {
    override def value_=(value: T)(implicit usr: AbstractUsr): Unit = super.value_=(value)
  }

  abstract class DBProp[T](initialValueOpt: Option[T] = None, pType: DBPropertyType = DBPropertyTypes.UNDEFINED) extends PropImpl[T](initialValueOpt, pType) {

    override final def value_=(value: T)(implicit usr: AbstractUsr): Unit = value_=(value, false)

    private[db] def value_=(value: T, internalUpdate: Boolean)(implicit usr: AbstractUsr): Unit = {
      super.value_=(value)
    }
  }

  /**
   * Property representing a mutable object in the database (can be set with the Int id or the object itself).
   */
  abstract class DBObjProp[T](initialValueOpt: Option[T] = None, pType: DBPropertyType = DBPropertyTypes.UNDEFINED) extends DBProp[T](initialValueOpt, pType) {

    def value_=(value: Int)(implicit usr: AbstractUsr): Unit
  }

}