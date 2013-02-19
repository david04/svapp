package com.github.david04.svapp.util

import vaadin.scala._
import vaadin.scala.Measure
import com.github.david04.svapp.base.SVApp


trait LayoutUtilsSVAppComponent {
  svApp: SVApp =>

  class LazyLoadingField[T](var dataVals: Seq[T], layoutFor: T => Layout, initStep: Int = 4, spacing: Boolean = true, discrete: Boolean = false) extends VerticalLayout {
    super.spacing = this.spacing

    private var step = initStep
    private val incrementRatio = 2

    val moreButton = new Button() {
      caption = "more"
      styleNames +=("theme", "link", "grey")
      clickListeners += (_ => more())
    }

    if (dataVals.isEmpty) {
      add(new Label() {
        value = "No entries to display"
        styleNames +=("theme", "grey")
      }, alignment = Alignment.MiddleCenter)
    } else {
      add(moreButton, alignment = if (discrete) Alignment.MiddleLeft else Alignment.MiddleCenter)
      more()
    }

    private def more() {
      val (show, rest) = dataVals.splitAt(step)
      step = (step * incrementRatio).toInt
      show.foreach(t => add(layoutFor(t), alignment = Alignment.MiddleCenter, index = components.size - 1))
      dataVals = rest
      if (dataVals.isEmpty) {
        removeComponent(moreButton)
        //      add(new Label() {value = "No more entries"; styleNames +=("theme", "grey")}, alignment = Alignment.MiddleCenter)
      }

    }
  }

  abstract class EditField(startsWithDisplay: Boolean = true) extends CssLayout {

    val switcher = ((_: Any) => switch())
    var editing = !startsWithDisplay
    add(
      if (startsWithDisplay)
        displayLayout()
      else
        editLayout())

    def display(): Unit = if (editing) switch()

    def edit(): Unit = if (!editing) switch()

    def switch() {
      removeAllComponents()
      if (editing) {
        add(displayLayout())
        editing = false
      } else {
        add(editLayout())
        editing = true
      }
    }

    protected def displayLayout(): Layout

    protected def editLayout(): Layout
  }

  abstract class ReloadableField(delayInit: Boolean = false) extends CssLayout {
    width = Measure(100, Units.pct)

    if (!delayInit) reload()

    def reload(): Unit = {
      removeAllComponents()
      add(displayLayout())
    }

    protected def displayLayout(): Layout
  }

  abstract class LoadingField(startsLoaded: Boolean = false) extends VerticalLayout {

    var isLoading = false
    if (startsLoaded) {
      removeAllComponents()
      add(displayLayout())
    } else {
      reload()
    }

    protected def load(finished: () => Unit)

    def loading(): Unit = svApp.synchronized {
      removeAllComponents()
      add(loadingLayout())
    }

    def reload(): Unit = {
      loading()
      load(() => svApp.synchronized {
        removeAllComponents()
        add(displayLayout())
      })
    }

    protected def loadingLayout(): Layout = new VerticalLayout() {
      add(new ProgressIndicator() {
        indeterminate = true
      }, alignment = Alignment.MiddleCenter)
      add(new Label() {
        width = None
        value = "Loading"
      }, alignment = Alignment.MiddleCenter)
    }

    protected def displayLayout(): Layout
  }

}