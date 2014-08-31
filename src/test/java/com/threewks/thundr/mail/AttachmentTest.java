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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.view.file.Disposition;

public class AttachmentTest {

	@Test
	public void shouldRetainNameViewAndDisposition() {
		Attachment attachment = new Attachment("name", "view", Disposition.Attachment);
		assertThat(attachment.name(), is("name"));
		assertThat(attachment.view(), is((Object) "view"));
		assertThat(attachment.disposition(), is(Disposition.Attachment));
		assertThat(attachment.isInline(), is(false));
		assertThat(attachment.contentId(), is("<name>"));
	}

	@Test
	public void shouldRetainNameViewAndDispositionWhenInline() {
		Attachment attachment = new Attachment("name", "view", Disposition.Inline);
		assertThat(attachment.name(), is("name"));
		assertThat(attachment.view(), is((Object) "view"));
		assertThat(attachment.disposition(), is(Disposition.Inline));
		assertThat(attachment.isInline(), is(true));
		assertThat(attachment.contentId(), is("<name>"));
	}

	@Test
	public void shouldHaveCorrectContentId() {
		assertThat(new Attachment("<name>", "view", Disposition.Inline).contentId(), is("<name>"));
		assertThat(new Attachment("name", "view", Disposition.Inline).contentId(), is("<name>"));
		assertThat(new Attachment("name>", "view", Disposition.Inline).contentId(), is("<name>"));
		assertThat(new Attachment("<name", "view", Disposition.Inline).contentId(), is("<name>"));
		assertThat(new Attachment("name and more", "view", Disposition.Inline).contentId(), is("<name and more>"));
	}
}
