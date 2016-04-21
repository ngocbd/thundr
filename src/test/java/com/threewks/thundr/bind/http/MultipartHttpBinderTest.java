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
package com.threewks.thundr.bind.http;

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.bind.http.MultipartHttpBinder.ThundrRequestContext;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.MultipartFile;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.test.TestSupport;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.util.Streams;

public class MultipartHttpBinderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private MultipartHttpBinder binder;
	private MockRequest request;
	private MockResponse response = new MockResponse();
	private Map<ParameterDescription, Object> parameterDescriptions;
	private ArrayList<FileItemStream> multipartData;
	private ParameterBinderRegistry parameterBinderRegistry;

	@Before
	public void before() throws FileUploadException, IOException {
		// @formatter:off
		request = new MockRequest(HttpMethod.POST, "/path/operation")
							.withContentType(ContentType.MultipartFormData);
		// @formatter:on
		parameterBinderRegistry = new ParameterBinderRegistry(TransformerManager.createWithDefaults());
		ParameterBinderRegistry.addDefaultBinders(parameterBinderRegistry);
		binder = new MultipartHttpBinder(parameterBinderRegistry);
		parameterDescriptions = new LinkedHashMap<ParameterDescription, Object>();

		multipartData = new ArrayList<FileItemStream>();
		FileUpload mockUpload = new FileUpload() {
			@Override
			public FileItemIterator getItemIterator(RequestContext ctx) throws FileUploadException, IOException {
				return new FileItemIterator() {
					Iterator<FileItemStream> iterator = multipartData.iterator();

					@Override
					public FileItemStream next() throws FileUploadException, IOException {
						return iterator.next();
					}

					@Override
					public boolean hasNext() throws FileUploadException, IOException {
						return iterator.hasNext();
					}
				};
			}
		};
		TestSupport.setField(binder, "upload", mockUpload);
	}

	@Test
	public void shouldOnlyBindMultipartContent() {
		request.withContentType(ContentType.ApplicationFormUrlEncoded);
		ParameterDescription field1 = new ParameterDescription("field1", String.class);
		ParameterDescription field2 = new ParameterDescription("field2", String.class);
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFormField("field3", "value3");
		parameterDescriptions.put(field1, null);
		parameterDescriptions.put(field2, null);

		binder.bindAll(parameterDescriptions, request, response);
		assertThat(parameterDescriptions.get(field1), is(nullValue()));
		assertThat(parameterDescriptions.get(field2), is(nullValue()));

		request.withContentType(ContentType.MultipartFormData);
		binder.bindAll(parameterDescriptions, request, response);
		assertThat(parameterDescriptions.get(field1), is(notNullValue()));
		assertThat(parameterDescriptions.get(field2), is(notNullValue()));
	}

	@Test
	public void shouldBindFormFieldsByDelegatingToHttpBinder() {
		ParameterDescription field1 = new ParameterDescription("field1", String.class);
		ParameterDescription field2 = new ParameterDescription("field2", String.class);
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFormField("field3", "value3");
		parameterDescriptions.put(field1, null);
		parameterDescriptions.put(field2, null);

		binder.bindAll(parameterDescriptions, request, response);

		assertThat(parameterDescriptions.get(field1), is((Object) "value1"));
		assertThat(parameterDescriptions.get(field2), is((Object) "value2"));
		assertThat(parameterDescriptions.size(), is(2));
	}

	@Test
	public void shouldBindByteArrayFromFileData() {
		ParameterDescription field1 = new ParameterDescription("field1", String.class);
		ParameterDescription field2 = new ParameterDescription("field2", String.class);
		ParameterDescription data = new ParameterDescription("data", byte[].class);
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFileField("data", new byte[] { 1, 2, 3 });
		parameterDescriptions.put(field1, null);
		parameterDescriptions.put(field2, null);
		parameterDescriptions.put(data, null);

		binder.bindAll(parameterDescriptions, request, response);

		assertThat(parameterDescriptions.get(field1), is((Object) "value1"));
		assertThat(parameterDescriptions.get(field2), is((Object) "value2"));
		assertThat(parameterDescriptions.get(data), is((Object) new byte[] { 1, 2, 3 }));
		assertThat(parameterDescriptions.size(), is(3));
	}

	@Test
	public void shouldBindInputStreamFromFileData() {
		ParameterDescription field1 = new ParameterDescription("field1", String.class);
		ParameterDescription field2 = new ParameterDescription("field2", String.class);
		ParameterDescription data = new ParameterDescription("data", InputStream.class);
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFileField("data", new byte[] { 1, 2, 3 });
		parameterDescriptions.put(field1, null);
		parameterDescriptions.put(field2, null);
		parameterDescriptions.put(data, null);

		binder.bindAll(parameterDescriptions, request, response);

		assertThat(parameterDescriptions.get(field1), is((Object) "value1"));
		assertThat(parameterDescriptions.get(field2), is((Object) "value2"));
		Object actualData = parameterDescriptions.get(data);
		assertThat(actualData, is(notNullValue()));
		assertThat(actualData instanceof InputStream, is(true));
		byte[] underlyingData = Streams.readBytes((InputStream) actualData);
		assertThat(underlyingData, is(new byte[] { 1, 2, 3 }));
		assertThat(parameterDescriptions.size(), is(3));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotBindIfAllParametersAreAlreadyBound() {
		ParameterDescription req = new ParameterDescription("req", Request.class);
		ParameterDescription resp = new ParameterDescription("resp", Response.class);
		ParameterDescription cookie = new ParameterDescription("cookie", String.class);
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFileField("data", new byte[] { 1, 2, 3 });
		parameterDescriptions.put(req, new MockRequest());
		parameterDescriptions.put(resp, new MockResponse());
		parameterDescriptions.put(cookie, "cookie-value");

		binder = spy(binder);
		binder.bindAll(parameterDescriptions, request, response);

		verify(binder, times(0)).extractParameters(Mockito.any(Request.class), Mockito.anyMap(), Mockito.anyMapOf(String.class, MultipartFile.class));

		assertThat(binder.shouldTryToBind(parameterDescriptions), is(false));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotBindIfNoParametersArePresent() {
		addFormField("field1", "value1");
		addFormField("field2", "value2");
		addFileField("data", new byte[] { 1, 2, 3 });

		binder = spy(binder);
		binder.bindAll(parameterDescriptions, request, response);

		verify(binder, times(0)).extractParameters(Mockito.any(Request.class), Mockito.anyMap(), Mockito.anyMapOf(String.class, MultipartFile.class));

		assertThat(binder.shouldTryToBind(parameterDescriptions), is(false));
	}

	@Test
	public void shouldFindRequestAsMultipart() {
		assertThat(ContentType.matchesAny("multipart/form-data; boundary=127.0.0.1.1002.16893.1359095066.582.122048", list(ContentType.MultipartFormData)), is(true));
	}

	@Test
	public void shouldThrowBindExceptionWhenFailsToBind() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind multipart form data: ");

		ParameterDescription data = new ParameterDescription("data", byte[].class);
		addFileField("data", null);
		parameterDescriptions.put(data, null);

		binder.bindAll(parameterDescriptions, request, response);
	}

	private void addFormField(final String name, final String value) {
		multipartData.add(new FileItemStream() {
			@Override
			public void setHeaders(FileItemHeaders headers) {
			}

			@Override
			public FileItemHeaders getHeaders() {
				return null;
			}

			@Override
			public InputStream openStream() throws IOException {
				return new ByteArrayInputStream(value.getBytes("UTF-8"));
			}

			@Override
			public boolean isFormField() {
				return true;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getFieldName() {
				return name;
			}

			@Override
			public String getContentType() {
				return null;
			}
		});
	}

	private void addFileField(final String name, final byte[] data) {
		multipartData.add(new FileItemStream() {
			@Override
			public void setHeaders(FileItemHeaders headers) {
			}

			@Override
			public FileItemHeaders getHeaders() {
				return null;
			}

			@Override
			public InputStream openStream() throws IOException {
				return new ByteArrayInputStream(data);
			}

			@Override
			public boolean isFormField() {
				return false;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public String getFieldName() {
				return name;
			}

			@Override
			public String getContentType() {
				return null;
			}
		});
	}

	@Test
	public void shouldMapThundrRequestToRequestContext() throws IOException {
		Request req = mock(Request.class);
		when(req.getCharacterEncoding()).thenReturn("UTF-7");
		when(req.getContentLength()).thenReturn(1234l);
		when(req.getContentTypeString()).thenReturn("content/type");
		when(req.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 0, 1, 2 }));
		
		ThundrRequestContext requestContext = new ThundrRequestContext(req);
		assertThat(requestContext.getCharacterEncoding(), is("UTF-7"));
		assertThat(requestContext.getContentLength(), is(1234));
		assertThat(requestContext.contentLength(), is(1234l));
		assertThat(requestContext.getContentType(), is("content/type"));
		assertThat(requestContext.getInputStream(), is(req.getInputStream()));
	}
}
