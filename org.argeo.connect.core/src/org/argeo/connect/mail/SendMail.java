package org.argeo.connect.mail;

import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectException;
import org.argeo.eclipse.ui.EclipseUiUtils;

/** Sends a mail via JavaMail, local mail command or Google Mail. */
public class SendMail implements Runnable {
	private final static Log log = LogFactory.getLog(SendMail.class);

	// See:
	// http://java.sun.com/developer/onlineTraining/JavaMail/contents.html#JavaMailUsage
	// http://java.sun.com/products/javamail/FAQ.html#gmail

	// Configure mail sending via system properties
	// TODO Use a proper deployment configuration mechanism
	public final static String ARGEO_SEND_MAILS = "org.argeo.connect.mail.dosend";
	private final static String ARGEO_SMTP_TLS_DEBUG = "org.argeo.connect.smtp.tls.debug";
	private final static String DEFAULT_ENCODING = "UTF-8";

	private final static boolean sendingActive = Boolean.parseBoolean(System.getProperty(ARGEO_SEND_MAILS, "true"));

	private String host;
	private String port;
	private String from;
	private String to;
	private String subject;
	private String plainText;
	private String htmlText;
	private String username;
	private String password;

	public void run() {
		// String doSend = System.getProperty(ARGEO_SEND_MAILS);

		if (!sendingActive)
			traceUnsentMail();
		else {
			if ("smtp.gmail.com".equals(host))
				sendWithGMail();
			else
				sendWithJavaSmtp();
		}
	}

	protected void sendWithJavaSmtp() {

		// See
		// http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/javamail/javamail.html
		String doDebugStr = System.getProperty(ARGEO_SMTP_TLS_DEBUG);
		boolean debug = "true".equals(doDebugStr);

		boolean auth = true;
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		// To enable sending email without certificate, using port 25
		props.put("mail.smtp.starttls.enable", false);
		// To enable sending email with certificate, using port 587
		// props.put("mail.smtp.starttls.enable", true);

		// switch (protocol) {
		// case SMTPS:
		// props.put("mail.smtp.ssl.enable", true);
		// break;
		// case TLS:
		// props.put("mail.smtp.starttls.enable", true);
		// break;
		// }

		Authenticator authenticator = null;
		if (auth) {
			props.put("mail.smtp.auth", true);
			authenticator = new Authenticator() {
				private PasswordAuthentication pa = new PasswordAuthentication(username, password);

				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return pa;
				}
			};
		}
		Session session = Session.getInstance(props, authenticator);
		session.setDebug(debug);
		MimeMessage message = new MimeMessage(session);
		try {
			buildJavaMailMessage(message);
			Transport.send(message);
		} catch (MessagingException ex) {
			throw new ConnectException("Unable to send java tls mail to " + to + " with username: " + username, ex);
		}
	}

	protected void sendWithGMail() {
		try {
			Properties props = new Properties();
			props.put("mail.smtps.auth", "true");
			props.put("mail.smtps.host", host);
			props.put("mail.smtp.starttls.enable", true);
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage message = new MimeMessage(session);
			buildJavaMailMessage(message);
			Transport t = session.getTransport("smtps");
			try {
				t.connect(host, username, password);
				t.sendMessage(message, message.getAllRecipients());
			} finally {
				t.close();
			}
			if (log.isDebugEnabled())
				log.debug("Sent mail to " + to + " with Google Mail");
		} catch (Exception e) {
			throw new ConnectException("Cannot send message.", e);
		}
	}

	protected void buildJavaMailMessage(MimeMessage message) throws MessagingException {
		message.setFrom(new InternetAddress(from));
		InternetAddress toAddr = new InternetAddress(to);
		message.setRecipient(Message.RecipientType.TO, toAddr);
		message.setSubject(subject, DEFAULT_ENCODING);
		if (EclipseUiUtils.notEmpty((htmlText))) {
			Multipart multipart = new MimeMultipart("alternative");
			// Plain text
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(htmlText, DEFAULT_ENCODING);
			// Html formatted
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setText(htmlText, DEFAULT_ENCODING, "html");
			// Attach to message
			multipart.addBodyPart(textPart);
			multipart.addBodyPart(htmlPart);
			message.setContent(multipart);
		} else
			message.setText(plainText, DEFAULT_ENCODING);
	}

	/**
	 * Shortcut to pass all data with only one call. We expect valid and non null
	 * strings for host, from, to, subject, text, username, password (See
	 * <code>MailProperty</code>
	 */
	public void setMailConfig(Map<String, String> mailConfig) {
		host = mailConfig.get(MailProperty.host.name());
		port = mailConfig.get(MailProperty.port.name());
		from = mailConfig.get(MailProperty.from.name());
		to = mailConfig.get(MailProperty.to.name());
		subject = mailConfig.get(MailProperty.subject.name());
		plainText = mailConfig.get(MailProperty.plainText.name());
		htmlText = mailConfig.get(MailProperty.htmlText.name());
		username = mailConfig.get(MailProperty.username.name());
		password = mailConfig.get(MailProperty.password.name());
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private void traceUnsentMail() {
		StringBuilder builder = new StringBuilder();
		builder.append("*** MAIL NOT SENT *** ");
		builder.append("This message should have been sent but mail sending is disabled.\n");
		builder.append(logMail(false));
		log.info(builder.toString());
	}

	public String logMail(boolean html) {
		StringBuilder builder = new StringBuilder();
		if (!html) {
			builder.append("From: ").append(from);
			builder.append(" - To: ").append(to);
			builder.append("- Subject: ").append(subject).append("\n");
			builder.append("Body: \n");
		}
		builder.append(html ? htmlText : plainText);
		return builder.toString();
	}

	public static boolean isSendingActive() {
		return sendingActive;
	}

}
