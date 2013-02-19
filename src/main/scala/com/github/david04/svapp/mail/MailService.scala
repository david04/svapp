package com.github.david04.svapp.mail

import javax.mail._
import javax.mail.internet._
import java.util.{Date, Properties}

import com.github.david04.svapp.base.SVApp

trait MailServiceSVAppComponent {
  svApp: SVApp =>

  val emailService = new MailService()

  class MailService {

    private val mailService = this
    private val props = new Properties()
    props.put("mail.smtp.auth", "" + svApp.conf.emailSmtpAuth)
    props.put("mail.smtp.starttls.enable", "" + svApp.conf.emailSmtpStarttls)
    props.put("mail.smtp.host", svApp.conf.emailSmtpHost)
    props.put("mail.smtp.port", "" + svApp.conf.emailSmtpPort)

    private val session: Session = Session.getInstance(props,
      new javax.mail.Authenticator() {
        override protected def getPasswordAuthentication(): PasswordAuthentication = {
          new PasswordAuthentication(svApp.conf.emailFrom, svApp.conf.emailPassword)
        }
      })

    def send(to: String, subject: String, text: String) {
      new Thread() {
        override def run() {
          mailService.synchronized {
            try {
              val message: Message = new MimeMessage(session)
              message.setFrom(new InternetAddress(svApp.conf.emailFrom))
              message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to).asInstanceOf[Array[javax.mail.Address]])
              message.setSubject(subject)
              message.setContent(text, "text/html; charset=utf-8")

              println("Sending email")
              Transport.send(message)
            } catch {
              case e: MessagingException => e.printStackTrace()
            }
          }
        }
      }.start()
    }

    def sendTemplate(template: EmailTemplate, email: Option[String] = None) {
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
          svApp.conf.supportEmail,
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