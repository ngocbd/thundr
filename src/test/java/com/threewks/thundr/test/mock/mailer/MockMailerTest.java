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
package com.threewks.thundr.test.mock.mailer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.mail.MailBuilder;
import com.threewks.thundr.mail.MailException;

public class MockMailerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private MockMailer mailer = new MockMailer();

	@Test
	public void shouldRetainReferenceToAllSentMailBuilders() {
		MailBuilder mail = mailer.mail().from("sender@email.com").to("recipient@email.com");
		assertThat(mailer.getSent().isEmpty(), is(true));

		mail.send();
		assertThat(mailer.getSent(), hasItem(mail));

		MailBuilder mail2 = mail.to("email");
		mail2.send();
		assertThat(mailer.getSent(), hasItems(mail, mail2));
	}

	@Test
	public void shouldPerformSenderValidation() {
		thrown.expect(MailException.class);
		thrown.expectMessage("No sender has been set for this email");
		mailer.mail().send();
	}

	@Test
	public void shouldPerformRecipientValidation() {
		thrown.expect(MailException.class);
		thrown.expectMessage("No recipient (to, cc or bcc) has been set for this email");
		mailer.mail().from("sender@email.com").send();
	}
}
