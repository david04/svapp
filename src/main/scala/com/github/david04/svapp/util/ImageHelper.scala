package com.github.david04.svapp.util

import java.awt.Image
import java.awt.image.BufferedImage
import java.io._
import javax.imageio.ImageIO
import vaadin.scala._
import mixins.{VerticalLayoutMixin, EmbeddedMixin}
import com.vaadin.server.StreamResource.StreamSource
import vaadin.scala.Measure
import com.vaadin.server.{DownloadStream, StreamResource}
import com.github.david04.svapp.base.SVApp

trait ImageHelperSVAppComponent {
  svApp: SVApp =>

  val imageHelper = new ImageHelper()

  class ImageHelper {

    val ACCOUNT_LOGO_HEIGHT = 44
    val EMAIL_ACCOUNT_LOGO_HEIGHT = 44
    val USER_ICON_SIZE = 24
    val EMAIL_USER_ICON_SIZE = 24
    val FILE_ICON_HEIGHT = 64
    val EMAIL_FILE_ICON_HEIGHT = 64

    def toBufferedImage(image: Image) = {
      val height = image.getHeight(null)
      val width = image.getWidth(null)
      val dim = (width, height)

      val bufferedImage = new BufferedImage(dim._1, dim._2, BufferedImage.TYPE_INT_ARGB)
      val g = bufferedImage.createGraphics()
      g.drawImage(image, 0, 0, dim._1, dim._2, null)
      g.dispose()
      bufferedImage
    }

    def imageScaled(image: BufferedImage, size: Int) = {
      val height = image.getHeight
      val width = image.getWidth
      val dim =
        if (width > height)
          (size, (size * (height / width.toDouble)).toInt)
        else
          ((size * (width.toDouble / height)).toInt, size)

      val scaledBI = new BufferedImage(dim._1, dim._2, BufferedImage.TYPE_INT_ARGB)
      val g = scaledBI.createGraphics()
      g.drawImage(image.getScaledInstance(dim._1, dim._2, Image.SCALE_SMOOTH), 0, 0, dim._1, dim._2, null)
      g.dispose()
      scaledBI
    }

    def imageScaledMaxHeight(image: BufferedImage, max: Int) = {
      val height = image.getHeight
      val width = image.getWidth
      val dim =
        if (height > max)
          ((max * (width.toDouble / height)).toInt, max)
        else
          (width, height)

      val scaledBI = new BufferedImage(dim._1, dim._2, BufferedImage.TYPE_INT_ARGB)
      val g = scaledBI.createGraphics()
      g.drawImage(image.getScaledInstance(dim._1, dim._2, Image.SCALE_SMOOTH), 0, 0, dim._1, dim._2, null)
      g.dispose()
      scaledBI
    }

    private val embeddedCache = collection.mutable.Map[BufferedImage, Array[Byte]]()

    def asEmbedded(img: BufferedImage): Embedded = {
      val embedded = svApp.appCache.getOrElseUpdate(img, {
        val ba = embeddedCache.getOrElseUpdate(img, {
          val os = new ByteArrayOutputStream()
          ImageIO.write(img, "png", os)
          os.toByteArray
        })
        new Embedded(new com.vaadin.ui.Embedded() with EmbeddedMixin {
          setSource(new StreamResource(new StreamSource {def getStream: InputStream = new ByteArrayInputStream(ba)}, "image-" + img.hashCode() + ".png"))
        })
      }).asInstanceOf[Embedded]
      if (embedded.parent.isDefined) {
        svApp.appCache.remove(img)
        asEmbedded(img)
      } else {
        embedded
      }
    }

    private val downloadStreamCache = collection.mutable.Map[BufferedImage, Array[Byte]]()

    def asDownloadStream(img: BufferedImage) = {
      val ba = downloadStreamCache.getOrElseUpdate(img, {
        val baos = new ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        baos.toByteArray
      })
      new DownloadStream(new ByteArrayInputStream(ba), "image/jpeg", img.hashCode() + ".jpg")
    }

    def changeImageLayout(
                           img: DBProp[BufferedImage],
                           resize: BufferedImage => BufferedImage,
                           display: BufferedImage => BufferedImage = (img) => img): Layout =
      new VerticalLayout(new com.vaadin.ui.VerticalLayout with VerticalLayoutMixin) {
        width = None
        spacing = true

        var baos: ByteArrayOutputStream = null

        val progressbar = new ProgressIndicator()
        progressbar.height = Measure(50, Units.pct)
        progressbar.pollingInterval = 1500

        val failL = new Label() {
          value = "Upload Failed"
          styleNames +=("theme", "red")
        }
        val processingL = new Button() {
          icon = new vaadin.scala.ThemeResource("layouts/its-brain/images/loaders/loader.gif")
        }

        val upload = new Upload() {
          immediate = true
          buttonCaption = "Change"
          styleNames +=("theme", "basic")
        }
        upload.receiver = (_ => {
          baos = new ByteArrayOutputStream()
          baos
        })

        upload.startedListeners.add({
          event: Upload.StartedEvent => {
            upload.visible = false
            add(ongoingDownloadLayout, alignment = Alignment.MiddleCenter)
          }
        })
        upload.progressListeners += (evt => progressbar.value = (evt.readBytes / evt.contentLength.toDouble))
        upload.succeededListeners += (evt => {
          removeComponent(ongoingDownloadLayout)
          add(processingL, alignment = Alignment.MiddleCenter)
          img.value = resize(ImageIO.read(new ByteArrayInputStream(baos.toByteArray)))
          removeComponent(processingL)
          upload.visible = true
          progressbar.value = 0.toDouble
        })
        upload.failedListeners += (evt => {
          removeComponent(ongoingDownloadLayout)
          upload.visible = true
          progressbar.value = 0.toDouble
        })

        val cancel = new Button() {
          caption = "Cancel"
          styleNames +=("theme", "red")
          clickListeners += (b => upload.interruptUpload())
        }
        val ongoingDownloadLayout = new HorizontalLayout() {
          spacing = true
          add(progressbar, alignment = Alignment.MiddleCenter)
          add(cancel, alignment = Alignment.MiddleCenter)
        }

        add(new HorizontalLayout() {
          img.addValueChangedListener(v => {
            removeAllComponents()
            add(imageHelper.asEmbedded(display(img.value)))
          })(this)
        }, alignment = Alignment.MiddleCenter)
        add(upload, alignment = Alignment.MiddleCenter)
      }
  }

  //  trait DBObjWithImg {
  //    self: DBObjectClassTrait =>
  //
  //    protected val _imgdata: String
  //    private val scalesCache = collection.mutable.Map[Symbol, BufferedImage]()
  //
  //    lazy val image = mutableWrappedVal[String, BufferedImage](
  //      _imgdata,
  //      "imgdata",
  //      DBPropertyTypes.UNDEFINED,
  //      (_: BufferedImage, _: BufferedImage) => scalesCache.clear(),
  //      (_: BufferedImage) => {},
  //      (b: BufferedImage) => DBObjectClassTrait.stringWrap(new ImageIcon(b)),
  //      (i: String) => imageHelper.toBufferedImage(DBObjectClassTrait.stringUnwrap[ImageIcon](i).getImage))
  //
  //    def imageUserIconSize = scalesCache.getOrElseUpdate('usericonsize, imageHelper.imageScaled(image.value, imageHelper.USER_ICON_SIZE))
  //
  //    def imageEmailUserIconSize = scalesCache.getOrElseUpdate('emailusericonsize, imageHelper.imageScaled(image.value, imageHelper.EMAIL_USER_ICON_SIZE))
  //
  //    def imageAccountLogoSize = scalesCache.getOrElseUpdate('accountlogosize, imageHelper.imageScaledMaxHeight(image.value, imageHelper.ACCOUNT_LOGO_HEIGHT))
  //
  //    def imageEmailAccountLogoSize = scalesCache.getOrElseUpdate('emailaccountlogosize, imageHelper.imageScaledMaxHeight(image.value, imageHelper.EMAIL_ACCOUNT_LOGO_HEIGHT))
  //
  //    def imageFileIconSize = scalesCache.getOrElseUpdate('fileiconsize, imageHelper.imageScaledMaxHeight(image.value, imageHelper.FILE_ICON_HEIGHT))
  //
  //    def imageEmailFileIconSize = scalesCache.getOrElseUpdate('emailfileiconsize, imageHelper.imageScaledMaxHeight(image.value, imageHelper.EMAIL_FILE_ICON_HEIGHT))
  //  }

}