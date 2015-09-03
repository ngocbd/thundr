/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
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

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.view.file.FileView;

public class MailBuilderImplTest {

	private Mailer mailer = mock(Mailer.class);
	private MailBuilderImpl builder = new MailBuilderImpl(mailer);
	private MailBuilderImpl originalInstance = builder;

	@Before
	public void before() {
	}

	@After
	public void after() {
	}

	@Test
	public void shouldRetainSubject() {
		assertThat(builder.subject(), is(nullValue()));
		builder = builder.subject("Subject");
		assertThat(builder.subject(), is("Subject"));
		assertThat(builder, not(sameInstance(originalInstance)));
	}

	@Test
	public void shouldRetainFrom() {
		assertThat(builder.from(), is(nullValue()));

		builder = builder.from("email@address.com");
		assertThat(builder.from(), is(entry("email@address.com")));

		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.from(), is(nullValue()));

		originalInstance = builder;
		builder = builder.from("email@address.com", "Emailer");
		assertThat(builder.from(), is(entry("email@address.com", "Emailer")));

		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.from(), is(entry("email@address.com")));
	}

	@Test
	public void shouldRetainReplyTo() {
		assertThat(builder.replyTo(), is(nullValue()));

		builder = builder.replyTo("email@address.com");
		assertThat(builder.replyTo(), is(entry("email@address.com")));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.replyTo(), is(nullValue()));

		originalInstance = builder;
		builder = builder.replyTo("email@address.com", "Emailer");
		assertThat(builder.replyTo(), is(entry("email@address.com", "Emailer")));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.replyTo(), is(entry("email@address.com")));
	}

	@Test
	public void shouldAddToRecipients() {
		assertThat(builder.to().isEmpty(), is(true));

		builder = builder.to("test@email1.com");
		assertThat(builder.to(), hasEntry("test@email1.com", null));

		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.to().isEmpty(), is(true));

		originalInstance = builder;
		builder = builder.to("test@email2.com", "Email Two");
		assertThat(builder.to(), hasEntry("test@email1.com", null));
		assertThat(builder.to(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.to(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.to(), not(hasEntry("test@email2.com", "Email Two")));

		Map<String, String> emails = map("test@email3.com", "Email Three", "test@email4.com", "Email Four");
		originalInstance = builder;
		builder = builder.to(emails);
		assertThat(builder.to(), hasEntry("test@email1.com", null));
		assertThat(builder.to(), hasEntry("test@email2.com", "Email Two"));
		assertThat(builder.to(), hasEntry("test@email3.com", "Email Three"));
		assertThat(builder.to(), hasEntry("test@email4.com", "Email Four"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.to(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.to(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance.to(), not(hasEntry("test@email3.com", "Email Three")));
		assertThat(originalInstance.to(), not(hasEntry("test@email4.com", "Email Four")));
	}

	@Test
	public void shouldAddCcRecipients() {
		assertThat(builder.cc().isEmpty(), is(true));
		builder = builder.cc("test@email1.com");
		assertThat(builder.cc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.cc().isEmpty(), is(true));

		originalInstance = builder;
		builder = builder.cc("test@email2.com", "Email Two");
		assertThat(builder.cc(), hasEntry("test@email1.com", null));
		assertThat(builder.cc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.cc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.cc(), not(hasEntry("test@email2.com", "Email Two")));

		Map<String, String> emails = map("test@email3.com", "Email Three", "test@email4.com", "Email Four");
		originalInstance = builder;
		builder = builder.cc(emails);
		assertThat(builder.cc(), hasEntry("test@email1.com", null));
		assertThat(builder.cc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(builder.cc(), hasEntry("test@email3.com", "Email Three"));
		assertThat(builder.cc(), hasEntry("test@email4.com", "Email Four"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.cc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.cc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance.cc(), not(hasEntry("test@email3.com", "Email Three")));
		assertThat(originalInstance.cc(), not(hasEntry("test@email4.com", "Email Four")));
	}

	@Test
	public void shouldAddBccRecipients() {
		assertThat(builder.bcc().isEmpty(), is(true));
		builder = builder.bcc("test@email1.com");
		assertThat(builder.bcc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.bcc().isEmpty(), is(true));

		originalInstance = builder;
		builder = builder.bcc("test@email2.com", "Email Two");
		assertThat(builder.bcc(), hasEntry("test@email1.com", null));
		assertThat(builder.bcc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.bcc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.bcc(), not(hasEntry("test@email2.com", "Email Two")));

		Map<String, String> emails = map("test@email3.com", "Email Three", "test@email4.com", "Email Four");
		originalInstance = builder;
		builder = builder.bcc(emails);
		assertThat(builder.bcc(), hasEntry("test@email1.com", null));
		assertThat(builder.bcc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(builder.bcc(), hasEntry("test@email3.com", "Email Three"));
		assertThat(builder.bcc(), hasEntry("test@email4.com", "Email Four"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.bcc(), hasEntry("test@email1.com", null));
		assertThat(originalInstance.bcc(), hasEntry("test@email2.com", "Email Two"));
		assertThat(originalInstance.bcc(), not(hasEntry("test@email3.com", "Email Three")));
		assertThat(originalInstance.bcc(), not(hasEntry("test@email4.com", "Email Four")));
	}

	@Test
	public void shouldRetainEmailBody() {
		assertThat(builder.body(), is(nullValue()));

		builder = builder.body("String");

		assertThat(builder.<String> body(), is("String"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.body(), is(nullValue()));
	}

	@Test
	public void shouldAddAttachments() throws Exception {
		builder = builder.attach(new FileView("test.txt", new byte[] {}, ContentType.TextPlain.value()));
		builder = builder.attach(new FileView("nyan.png", new byte[] {}, ContentType.ImagePng.value()));

		assertThat(builder.attachments().size(), is(2));
		assertThat(builder.attachments().get(0).name(), is("test.txt"));
		assertThat(builder.attachments().get(1).name(), is("nyan.png"));
		assertThat(originalInstance, not(sameInstance(builder)));
		assertThat(originalInstance.attachments().isEmpty(), is(true));
	}

	@Test
	public void shouldInvokeMailerOnSend() {
		builder.send();
		verify(mailer, times(1)).send(builder);
	}

	public static final Map.Entry<String, String> entry(String email) {
		return Collections.singletonMap(email, (String) null).entrySet().iterator().next();
	}

	public static final Map.Entry<String, String> entry(String email, String name) {
		return Collections.singletonMap(email, name).entrySet().iterator().next();
	}
}
