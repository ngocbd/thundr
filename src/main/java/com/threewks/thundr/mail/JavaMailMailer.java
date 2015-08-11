/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
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

import java.util.ArrayList;
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
import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.InMemoryResponse;
import com.threewks.thundr.request.RequestContainer;
import com.threewks.thundr.view.BasicViewRenderer;
import com.threewks.thundr.view.ViewResolverRegistry;

import jodd.util.Base64;

/**
 * An implementation of {@link Mailer} which uses javax.mail.
 */
public class JavaMailMailer extends BaseMailer {

	public JavaMailMailer(ViewResolverRegistry viewResolverRegistry, RequestContainer requestContainer) {
		super(viewResolverRegistry, requestContainer);
	}

	@Override
	protected void sendInternal(Map.Entry<String, String> from, Map.Entry<String, String> replyTo, Map<String, String> to, Map<String, String> cc, Map<String, String> bcc, String subject,
			Object body, List<Attachment> attachments) {
		try {
			Session emailSession = getSession();

			Message message = new MimeMessage(emailSession);
			message.setFrom(emailAddress(from));
			if (replyTo != null) {
				message.setReplyTo(new Address[] { emailAddress(replyTo) });
			}

			message.setSubject(subject);

			InMemoryResponse renderedResult = render(body);
			String content = renderedResult.getBodyAsString();
			String contentType = ContentType.cleanContentType(renderedResult.getContentTypeString());
			contentType = StringUtils.isBlank(contentType) ? ContentType.TextHtml.value() : contentType;

			if (Expressive.isEmpty(attachments)) {
				message.setContent(content, contentType);
			} else {
				Multipart multipart = new MimeMultipart("mixed"); // subtype must be "mixed" or inline & regular attachments won't play well together
				addBody(multipart, content, contentType);
				addAttachments(multipart, attachments);
				message.setContent(multipart);
			}

			addRecipients(to, message, RecipientType.TO);
			addRecipients(cc, message, RecipientType.CC);
			addRecipients(bcc, message, RecipientType.BCC);

			sendMessage(message);
		} catch (MessagingException e) {
			throw new MailException(e, "Failed to send an email: %s", e.getMessage());
		}
	}

	protected Session getSession() {
		return Session.getDefaultInstance(new Properties());
	}

	protected void sendMessage(Message message) throws MessagingException {
		Transport.send(message);
	}

	private void addBody(Multipart multipart, String content, String contentType) throws MessagingException {
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(content, contentType);
		multipart.addBodyPart(messageBodyPart);
	}

	private void addRecipients(Map<String, String> to, Message message, RecipientType recipientType) throws MessagingException {
		if (Expressive.isNotEmpty(to)) {
			List<Address> addresses = new ArrayList<>();
			for (Entry<String, String> recipient : to.entrySet()) {
				addresses.add(emailAddress(recipient));
			}
			message.addRecipients(recipientType, addresses.toArray(new Address[0]));
		}
	}

	private void addAttachments(Multipart multipart, List<Attachment> attachments) throws MessagingException {
		for (Attachment attachment : attachments) {
			InMemoryResponse renderedResult = render(attachment.view());
			byte[] base64Encoded = Base64.encodeToByte(renderedResult.getBodyAsBytes());

			InternetHeaders headers = new InternetHeaders();
			headers.addHeader(Header.ContentType, renderedResult.getContentTypeString());
			headers.addHeader(Header.ContentTransferEncoding, "base64");

			MimeBodyPart part = new MimeBodyPart(headers, base64Encoded);
			part.setFileName(attachment.name());
			part.setDisposition(attachment.disposition().value());

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
