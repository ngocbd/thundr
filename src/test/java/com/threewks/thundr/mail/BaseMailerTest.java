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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.exception.BaseException;
import com.threewks.thundr.http.RequestThreadLocal;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.ThreadLocalRequestContainer;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverRegistry;
import com.threewks.thundr.view.file.Disposition;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class BaseMailerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private ThreadLocalRequestContainer requestContainer = new ThreadLocalRequestContainer();
	private BaseMailer mailer = spy(new BaseMailer(viewResolverRegistry, requestContainer) {
		@Override
		protected void sendInternal(Entry<String, String> from, Entry<String, String> replyTo, Map<String, String> to, Map<String, String> cc, Map<String, String> bcc, String subject, Object body,
				List<Attachment> attachments) {
		}
	});
	private MockHttpServletRequest req = new MockHttpServletRequest();

	@Before
	public void before() throws MessagingException {
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());
		RequestThreadLocal.set(req, null);
	}

	@After
	public void after() {
		RequestThreadLocal.clear();
	}

	@Test
	public void shouldSendEmailUsingWithEmailFields() throws MessagingException {
		StringView body = new StringView("Email body").withContentType("text/plain");
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
		verify(mailer).sendInternal(entry("test@email.com"), entry("reply@email.com"), email("recipient@email.com"), email("cc@email.com"), email("bcc@email.com"), "Subject", body,
				Collections.<Attachment> emptyList());
	}

	@Test
	public void shouldSendEmailWithAttachments() throws MessagingException {
		StringView body = new StringView("Email body").withContentType("text/plain");
		// @formatter:off
		MailBuilder builder = mailer.mail()
			.from("test@email.com")
			.replyTo("reply@email.com")
			.to("recipient@email.com")
			.cc("cc@email.com")
			.bcc("bcc@email.com")
			.body(body)
			.subject("Subject")
			.attach("image1.jpg", "Image view", Disposition.Inline)
			.attach("attachment", "Content", Disposition.Attachment)
			.attach("attachment", "Different content, same name", Disposition.Attachment);
		// @formatter:on
		builder.send();

		List<Attachment> expectedAttachments = Arrays.asList(new Attachment("image1.jpg", "Image view", Disposition.Inline), new Attachment("attachment", "Content", Disposition.Attachment),
				new Attachment("attachment", "Different content, same name", Disposition.Attachment));

		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("test@email.com"), entry("reply@email.com"), email("recipient@email.com"), email("cc@email.com"), email("bcc@email.com"), "Subject", body, expectedAttachments);
	}

	@Test
	public void shouldSendBasicEmailUsingNames() throws MessagingException {
		StringView body = new StringView("Email body").withContentType("text/plain");
		Map<String, String> to = Expressive.map("steve@place.com", "Steve", "john@place.com", "John");
		// @formatter:off
		MailBuilder builder = mailer.mail()
				.from("sender@email.com", "System Name")
				.to(to)
				.body(body)
				.subject("Subject line");
		// @formatter:on
		builder.send();

		verify(mailer).send(builder);
		verify(mailer).sendInternal(entry("sender@email.com", "System Name"), null, to, empty(), empty(), "Subject line", body, Collections.<Attachment> emptyList());
	}

	@Test
	public void shouldThrownMailExceptionIfSendInternalFails() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("Failed to send an email: expected message");

		doThrow(new RuntimeException("expected message")).when(mailer).sendInternal(anyEntry(), anyEntry(), anyMap(), anyMap(), anyMap(), anyString(), any(), anyListOf(Attachment.class));

		StringView body = new StringView("Email body").withContentType("text/plain");

		// @formatter:off
		MailBuilder builder = mailer.mail()
			.from("sender@email.com", "System Name")
			.to("steve@place.com", "Steve")
			.body(body)
			.subject("Subject line");
		// @formatter:on
		builder.send();

		verify(mailer).sendInternal(entry("sender@email.com", "System Name"), null, email("steve@place.com", "Steve"), empty(), empty(), "Subject line", body, Collections.<Attachment> emptyList());
	}

	@Test
	public void shouldThrownMailExceptionIfFromIsMissing() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("No sender has been set for this email");

		MailBuilder builder = mailer.mail();
		builder.to("steve@place.com", "Steve");
		builder.send();
	}

	@Test
	public void shouldThrownMailExceptionIfFromEmailIsNull() throws MessagingException {
		thrown.expect(MailException.class);
		thrown.expectMessage("No sender has been set for this email");

		MailBuilder builder = mailer.mail();
		builder.from(null);
		builder.to("steve@place.com", "Steve");
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
	public void shouldThrowMailExceptionIfViewResolutionFails() {
		thrown.expect(MailException.class);
		thrown.expectMessage("Failed to render email part: No ViewResolver is registered for the view result String");

		mailer.render("No resolver registered for strings");
	}

	@Test
	public void shouldNotFailToRenderBodyIfNoHttpServletRequestSupplied() {
		RequestThreadLocal.clear();
		StringView body = new StringView("Email body").withContentType((String) null);
		// @formatter:off
		MailBuilder builder = mailer.mail()
				.from("sender@email.com")
				.to("recipient@email.com")
				.body(body)
				.subject("Subject line");
		// @formatter:on
		builder.send();

		verify(mailer).sendInternal(entry("sender@email.com"), null, email("recipient@email.com"), empty(), empty(), "Subject line", body, Collections.<Attachment> emptyList());
	}

	@Test
	public void shouldNotFailIfTheMailBuilderIsNotAMailBuilderImpl() {
		StringView body = new StringView("Email body");
		MailBuilder builder = mock(MailBuilder.class);
		when(builder.from()).thenReturn(entry("sender@email.com"));
		when(builder.to()).thenReturn(email("recipient@email.com"));
		when(builder.body()).thenReturn(body);
		when(builder.subject()).thenReturn("Subject line");
		mailer.send(builder);

		verify(mailer).sendInternal(entry("sender@email.com"), null, email("recipient@email.com"), empty(), empty(), "Subject line", body, Collections.<Attachment> emptyList());
	}

	@Test
	public void shouldSaveThenRestoreAttributesOnRequestDuringContentRendering() {
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request request, Response resp, String viewResult) {
				HttpServletRequest req = request.getRawRequest(HttpServletRequest.class);
				req.setAttribute("updated", 2);
				req.removeAttribute("initial");
				try {
					resp.getOutputStream().write(viewResult.getBytes("UTF-8"));
				} catch (IOException e) {
					throw new BaseException(e);
				}
				assertThat(BaseMailerTest.this.req.getAttribute("initial"), is(nullValue()));
				assertThat(BaseMailerTest.this.req.getAttribute("updated"), is((Object) 2));
			}
		});

		req.setAttribute("initial", "value");
		req.setAttribute("updated", 1);

		assertThat(req.getAttribute("initial"), is((Object) "value"));
		assertThat(req.getAttribute("updated"), is((Object) 1));

		mailer.mail()
			.from("sender@email.com")
			.to("recipient@email.com")
			.body("Email body")
			.subject("Subject line")
			.send();

		verify(mailer).sendInternal(entry("sender@email.com"), null, email("recipient@email.com"), empty(), empty(), "Subject line", "Email body", Collections.<Attachment> emptyList());

		assertThat(req.getAttribute("initial"), is((Object) "value"));
		assertThat(req.getAttribute("updated"), is((Object) 1));

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

	@SuppressWarnings("unchecked")
	public static final Map.Entry<String, String> anyEntry() {
		return Mockito.any(Map.Entry.class);
	}

	public static final Map<String, String> anyMap() {
		return Mockito.anyMapOf(String.class, String.class);
	}
}
