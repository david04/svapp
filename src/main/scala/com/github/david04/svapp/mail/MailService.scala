package com.github.david04.svapp.mail

import javax.mail._
import javax.mail.internet._
import java.util.{Date, Properties}

import com.github.david04.svapp.base.{SVAppDB, SVApp}
import javax.activation.{DataHandler, FileDataSource}
import java.io.File

trait MailServiceSVAppComponent {
  svApp: SVAppDB =>

  val emailService = new MailService()

  class MailService {

    private val mailService = this
    private val props = new Properties()
    props.put("mail.smtp.auth", "" + conf.emailSmtpAuth)
    props.put("mail.smtp.starttls.enable", "" + conf.emailSmtpStarttls)
    props.put("mail.smtp.host", conf.emailSmtpHost)
    props.put("mail.smtp.port", "" + conf.emailSmtpPort)

    private val session: Session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        override protected def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication(svApp.conf.emailFrom, svApp.conf.emailPassword)
        }
      })

    def send(to: String, subject: String, text: String, html: Boolean = true, attachments: Seq[File] = Seq()): Unit =
      sendEmail(to, subject, text, html, attachments.map(f => (f, f.getName)))

    def sendEmail(to: String, subject: String, text: String, html: Boolean, attachments: Seq[(File, String)]): Unit = {
      new Thread() {
        override def run() {
          mailService.synchronized {
            try {
              val message: Message = new MimeMessage(session)
              message.setFrom(new InternetAddress(svApp.conf.emailFrom))
              message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to).asInstanceOf[Array[javax.mail.Address]])
              message.setSubject(subject)
              //              message.setContent(text, if (html) "text/html; charset=utf-8" else "text/plain; charset=utf-8")

              val mbp1 = new MimeBodyPart()
              mbp1.setContent(text, if (html) "text/html; charset=utf-8" else "text/plain; charset=utf-8")

              val files = attachments.map(f => {
                val mbp = new MimeBodyPart()
                val fds = new FileDataSource(f._1) {
                  override def getContentType =
                    "application/octet-stream"
                }
                mbp.setDataHandler(new DataHandler(fds))
                mbp.setFileName(f._2)
                mbp.setHeader("Content-Type", fds.getContentType())
                mbp.setHeader("Content-ID", f._2)
                mbp.setDisposition(Part.INLINE)
//                fds.getOutputStream.close
                mbp
              })

              val mp = new MimeMultipart()
              mp.addBodyPart(mbp1)
              files.foreach(mp.addBodyPart(_))

              message.setContent(mp)

              println("Sending email")
              Transport.send(message)
            } catch {
              case e: MessagingException => e.printStackTrace()
            }
          }
        }
      }.start()
    }

    def sendTemplate(template: EmailTemplate, email: Option[String] = None, usr: AbstractUsr) {
      if (template.shouldSend()) {
        val subject = template.subject()
        val html = SimpleLetterheadLeftlogoLayout.html(
          "" + (new Date().getYear + 1900),
          usr.name,
          template.preview(),
          (template.onlineSrc() match {
            case Some(src) => {
              "Email not displaying correctly?" + "<br/>" +
                SimpleLetterheadLeftlogoLayout.a(src, "Access online") + "."
            }
            case None => ""
          }),
          "",
          template.mainTitle(),
          template.sideContent(),
          template.mainContent(),
          List(),
          conf.supportEmail,
          List()
        )
        send(email match {
          case Some(mail) => mail
          case None => usr.email.value
        }, subject, html)
      }
    }
  }

}