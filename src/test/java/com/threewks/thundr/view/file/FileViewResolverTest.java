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
package com.threewks.thundr.view.file;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.view.ViewResolutionException;

public class FileViewResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private MockRequest req = new MockRequest();
	private MockResponse resp = new MockResponse();
	private byte[] data = new byte[] { 1, 2, 3 };
	private FileView fileView = new FileView("filename.ext", data, "content/type");
	private FileViewResolver fileViewResolver = new FileViewResolver();

	@Test
	public void shouldWriteContentTypeAndFilenameToHeaders() {
		fileViewResolver.resolve(req, resp, fileView);

		assertThat(resp.getContentTypeString(), is("content/type"));
		assertThat(resp.getHeader(Header.ContentDisposition), is("attachment; filename=filename.ext"));
	}

	@Test
	public void shouldWriteDispositionToHeaders() {
		fileView.withDisposition(Disposition.Inline);
		fileViewResolver.resolve(req, resp, fileView);

		assertThat(resp.getContentTypeString(), is("content/type"));
		assertThat(resp.getHeader(Header.ContentDisposition), is("inline; filename=filename.ext"));
	}

	@Test
	public void shouldAllowDispositionHeaderToBeOverriddeByExtendedHeaders() {
		fileView.withHeader(Header.ContentDisposition, "something-else");
		fileViewResolver.resolve(req, resp, fileView);

		assertThat(resp.getContentTypeString(), is("content/type"));
		assertThat(resp.getHeader(Header.ContentDisposition), is("something-else"));
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenFailedToWriteToOutputStream() {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Failed to write FileView result: Expected exception");

		fileView = spy(fileView);
		when(fileView.getContentType()).thenThrow(new RuntimeException("Expected exception"));

		fileViewResolver.resolve(req, resp, fileView);
	}

	@Test
	public void shouldRespectExtendedViewValues() {

		Cookie cookie = Cookie.build("cookie").withValue("value2").build();
		fileView.withContentType("content/type").withCharacterEncoding("UTF-16").withHeader("header", "value1").withCookie(cookie);

		fileViewResolver.resolve(req, resp, fileView);
		assertThat(resp.getContentTypeString(), is("content/type"));
		assertThat(resp.getCharacterEncoding(), is("UTF-16"));
		assertThat(resp.getHeader("header"), is("value1"));
		assertThat(resp.getCookies(), hasItem(cookie));
	}

	@Test
	public void shouldReturnClassNameForToString() {
		assertThat(new FileViewResolver().toString(), is("FileViewResolver"));
	}

	@Test
	public void shouldCloseInputStream() throws IOException {
		InputStream is = mockInputStream();

		fileView = new FileView("filename.ext", is, "content/type");
		fileViewResolver.resolve(req, resp, fileView);

		verify(is).close();
	}

	@Test
	public void shouldNotThrowExceptionIfClosingInputStreamFails() throws IOException {
		InputStream is = mockInputStream();

		doThrow(new IOException("intentional")).when(is).close();

		fileView = new FileView("filename.ext", is, "content/type");
		fileViewResolver.resolve(req, resp, fileView);

		verify(is).close();
	}

	public InputStream mockInputStream() throws IOException {
		InputStream is = mock(InputStream.class);
		when(is.read()).thenReturn(-1);
		when(is.read(Mockito.any(byte[].class))).thenReturn(-1);
		when(is.read(Mockito.any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
		return is;
	}
}
