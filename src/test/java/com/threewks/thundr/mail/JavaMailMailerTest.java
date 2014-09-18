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

import static com.threewks.thundr.mail.MailBuilderImplTest.entry;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.http.RequestThreadLocal;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.view.ViewResolverRegistry;
import com.threewks.thundr.view.file.Disposition;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class JavaMailMailerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private JavaMailMailer mailer = new JavaMailMailer(viewResolverRegistry);
	private MockHttpServletRequest req = new MockHttpServletRequest();
	private ArrayList<Attachment> NoAttachments = new ArrayList<Attachment>();

	@Before
	public void before() throws MessagingException {
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());

		mailer = spy(mailer);
		doNothing().when(mailer).sendMessage(Mockito.any(Message.class));
		RequestThreadLocal.set(req, null);
	}

	@After
	public void after() {
		RequestThreadLocal.clear();
	}

	@Test
	public void shouldSendEmailusingJavaMailWithEmailFields() throws MessagingException {
		StringView body = new StringView("Email body");
		// @formatter:off
		MailBuilder builder = mailer.mail()
				.from("test@email.com")
				.replyTo("reply@email.com")
				.to("recipient@email.com")
				.cc("cc@email.com")
				.bcc("bcc@email.com")
				.body(body)
				.subject("Subject");
		// @formatter:on
		builder.send();

		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("test@email.com"), entry("reply@email.com"), email("recipient@email.com"), email("cc@email.com"), email("bcc@email.com"), "Subject", body, NoAttachments);
		verify(mailer).sendMessage(Mockito.any(Message.class));
	}

	@Test
	public void shouldSendBasicEmailUsingNames() throws MessagingException {
		StringView body = new StringView("Email body");
		Map<String, String> to = Expressive.map("steve@place.com", "Steve", "john@place.com", "John");
		MailBuilder builder = mailer.mail().from("sender@email.com", "System Name").to(to).body(body).subject("Subject line");
		builder.send();

		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("sender@email.com", "System Name"), null, to, empty(), empty(), "Subject line", body, NoAttachments);
		verify(mailer).sendMessage(Mockito.any(Message.class));
	}

	@Test
	public void shouldSendEmailWithAttachment() throws Exception {
		StringView body = new StringView("Email body");

		Map<String, String> to = Expressive.map("foo@example.org", "Foo");
		// @formatter:off
		MailBuilder builder = mailer.mail()
			.from("sender@example.org", "Sender")
			.to(to)
			.body(body)
			.subject("Subject line")
			.attach("test.txt", new StringView("Blah"), Disposition.Attachment);
		// @formatter:on
		builder.send();

		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("sender@example.org", "Sender"), null, to, empty(), empty(), "Subject line", body, builder.attachments());
		verify(mailer).sendMessage(Mockito.any(Message.class));
	}
	@Test
	public void shouldSendEmailWithInlineAttachment() throws Exception {
		StringView body = new StringView("Email body");
		
		Map<String, String> to = Expressive.map("foo@example.org", "Foo");
		// @formatter:off
		MailBuilder builder = mailer.mail()
				.from("sender@example.org", "Sender")
				.to(to)
				.body(body)
				.subject("Subject line")
				.attach("test.txt", new StringView("Blah"), Disposition.Inline);
		// @formatter:on
		builder.send();
		
		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("sender@example.org", "Sender"), null, to, empty(), empty(), "Subject line", body, builder.attachments());
		verify(mailer).sendMessage(Mockito.any(Message.class));
	}

	@Test
	public void shouldThrownMailExceptionIfJavaMAilSendFails() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("Failed to send an email: expected message");

		doThrow(new MessagingException("expected message")).when(mailer).sendMessage(Mockito.any(Message.class));
		StringView body = new StringView("Email body");

		mailer.mail().from("sender@email.com", "System Name").to("steve@place.com", "Steve").body(body).subject("Subject line").send();

		verify(mailer).sendInternal(entry("sender@email.com", "System Name"), null, email("steve@place.com", "Steve"), empty(), empty(), "Subject line", body, NoAttachments);
	}

	@Test
	public void shouldThrownMailExceptionIfFromIsMissing() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("No sender has been set for this email");

		MailBuilder builder = mailer.mail();
		builder.to("steve@place.com", "Steve");
		builder.body(new StringView("Email body").withContentType("text/plain"));
		builder.subject("Subject line");
		builder.send();
	}

	@Test
	public void shouldThrownMailExceptionIfNoRecipientIsDefined() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("No recipient (to, cc or bcc) has been set for this email");

		// @formatter:off
		mailer.mail()
			.from("sender@place.com", "Steve")
			.body(new StringView("Email body").withContentType("text/plain"))
			.subject("Subject line")
			.send();
		// @formatter:on
	}

	@Test
	public void shouldThrownMailExceptionIfAnInvalidEmailAddressIsSpecified() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("Failed to send an email - unable to set a sender or recipient of 'ju nk' <null>:");

		// @formatter:off
		mailer.mail()
			.from("ju nk")
			.to("steve", "Steve")
			.body(new StringView("Email body").withContentType("text/plain"))
			.subject("Subject line")
			.send();
		// @formatter:on
	}

	@Test
	public void shouldThrowMailExceptionIfViewResolutionFails() {
		thrown.expect(MailException.class);
		thrown.expectMessage("Failed to render email part: No ViewResolver is registered for the view result String");

		mailer.render("No resolver registered for strings");
	}

	public static final Map<String, String> email(String email) {
		return Expressive.map(email, null);
	}

	public static final Map<String, String> email(String email, String name) {
		return Expressive.map(email, name);
	}

	public static final Map<String, String> empty() {
		return Expressive.map();
	}
}
