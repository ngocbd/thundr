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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.ContentType;

public class AttachmentBuilderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldBuildAttachmentFromInputStream() throws Exception {
		String name = "test.txt";
		byte[] data = new byte[1024];

		Attachment attachment = new AttachmentBuilder()
				.name(name)
				.contentType(ContentType.TextPlain)
				.data(data)
				.build();

		assertThat(attachment.getName(), is(name));
		assertThat(attachment.getContentType(), is(ContentType.TextPlain.value()));
		assertThat(attachment.getData(), is(data));
	}

	@Test
	public void shouldBuildAttachmentFromByteArray() throws Exception {
		String name = "test.txt";

		Attachment attachment = new AttachmentBuilder()
				.name(name)
				.contentType(ContentType.TextPlain)
				.data(new byte[]{})
				.build();

		assertThat(attachment.getName(), is(name));
		assertThat(attachment.getContentType(), is(ContentType.TextPlain.value()));
		assertThat(attachment.getData(), instanceOf(byte[].class));
	}

	@Test
	public void shouldBuildAttachmentFromFile() throws Exception {
		String name = "test.txt";

		Attachment attachment = new AttachmentBuilder()
				.name(name)
				.contentType(ContentType.TextPlain)
				.data(new File(getClass().getResource("/LICENSE").toURI()))
				.build();

		assertThat(attachment.getName(), is(name));
		assertThat(attachment.getContentType(), is(ContentType.TextPlain.value()));
		assertThat(attachment.getData(), instanceOf(byte[].class));
	}

	@Test
	public void shouldThrowExceptionOnBuildIfFileDoesNotExist() throws Exception {
		thrown.expect(MailException.class);

		String name = "test.txt";

		new AttachmentBuilder()
				.name(name)
				.contentType(ContentType.TextPlain)
				.data(new File(name))
				.build();
	}

	@Test
	public void shouldThrowExceptionOnBuildIfNameNotSet() throws Exception {
		thrown.expect(MailException.class);

		new AttachmentBuilder()
				.contentType(ContentType.TextPlain)
				.data(mock(InputStream.class))
				.build();
	}

	@Test
	public void shouldThrowExceptionOnBuildIfContentTypeNotSet() throws Exception {
		thrown.expect(MailException.class);

		new AttachmentBuilder()
				.name("test.txt")
				.data(mock(InputStream.class))
				.build();
	}

	@Test
	public void shouldThrowExceptionOnBuildIfDataNotSet() throws Exception {
		thrown.expect(MailException.class);

		new AttachmentBuilder()
				.name("test.txt")
				.contentType(ContentType.TextPlain)
				.build();
	}
}