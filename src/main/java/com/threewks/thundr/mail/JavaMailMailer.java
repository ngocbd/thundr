/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.mail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.SyntheticHttpServletResponse;
import com.threewks.thundr.view.ViewResolverRegistry;

import jodd.util.Base64;

/**
 * An implementation of {@link Mailer} which uses javax.mail.
 */
public class JavaMailMailer extends BaseMailer {

	public JavaMailMailer(ViewResolverRegistry viewResolverRegistry) {
		super(viewResolverRegistry);
	}

	@Override
	protected void sendInternal(Map.Entry<String, String> from, Map.Entry<String, String> replyTo, Map<String, String> to, Map<String, String> cc, Map<String, String> bcc, String subject,
			String content, String contentType, List<Attachment> attachments) {
		try {
			Session emailSession = Session.getDefaultInstance(new Properties());

			Message message = new MimeMessage(emailSession);
			message.setFrom(emailAddress(from));
			if (replyTo != null) {
				message.setReplyTo(new Address[] { emailAddress(replyTo) });
			}
			addRecipients(to, message, RecipientType.TO);
			addRecipients(cc, message, RecipientType.CC);
			addRecipients(bcc, message, RecipientType.BCC);

			message.setSubject(subject);

			Multipart multipart = new MimeMultipart("mixed");  // subtype must be "mixed" or inline & regular attachments won't play well together
			addBody(multipart, content, contentType);
			addAttachments(multipart, attachments);
			message.setContent(multipart);

			sendMessage(message);
		} catch (MessagingException e) {
			throw new MailException(e, "Failed to send an email: %s", e.getMessage());
		} catch (IOException e) {
			throw new MailException(e, "Failed to send an email: %s", e.getMessage());
		}
	}

	protected void sendMessage(Message message) throws MessagingException {
		Transport.send(message);
	}

	private void addBody(Multipart multipart, String content, String contentType) throws MessagingException {
		if (ContentType.TextHtml.value().equals(contentType)) {
			BodyPart textPart = new MimeBodyPart();
			textPart.setText("This message is intended for viewing with an HTML capable email client.");
			multipart.addBodyPart(textPart);
		}

		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(content, contentType);
		multipart.addBodyPart(messageBodyPart);
	}

	private void addRecipients(Map<String, String> to, Message message, RecipientType recipientType) throws MessagingException {
		if (Expressive.isNotEmpty(to)) {
			for (Entry<String, String> recipient : to.entrySet()) {
				message.setRecipient(recipientType, emailAddress(recipient));
			}
		}
	}

	private void addAttachments(Multipart multipart, List<Attachment> attachments) throws MessagingException, IOException {
		for (Attachment attachment : attachments) {
			InternetHeaders headers = new InternetHeaders();
			headers.addHeader("Content-Type", attachment.contentType());
			headers.addHeader("Content-Transfer-Encoding", "base64");

			SyntheticHttpServletResponse response = render(attachment.view());
			byte[] base64Encoded = Base64.encodeToByte(response.getResponseContentAsBytes());

			MimeBodyPart part = new MimeBodyPart(headers, base64Encoded);
			part.setFileName(attachment.name());
			part.setDisposition(attachment.disposition().getValue());

			if (attachment.isInline()) {
				part.setContentID(attachment.contentId());
			}

			multipart.addBodyPart(part);
		}
	}

	private InternetAddress emailAddress(Map.Entry<String, String> entry) {
		try {
			return StringUtils.isBlank(entry.getValue()) ? new InternetAddress(entry.getKey()) : new InternetAddress(entry.getKey(), entry.getValue());
		} catch (Exception e) {
			throw new MailException(e, "Failed to send an email - unable to set a sender or recipient of '%s' <%s>: %s", entry.getKey(), entry.getValue(), e.getMessage());
		}
	}
}
