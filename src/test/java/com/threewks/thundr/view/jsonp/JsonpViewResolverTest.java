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
package com.threewks.thundr.view.jsonp;

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.view.ViewResolutionException;

public class JsonpViewResolverTest {

	@Rule public ExpectedException thrown = ExpectedException.none();

	private MockRequest req = new MockRequest();
	private MockResponse resp = new MockResponse();
	private JsonpViewResolver resolver = new JsonpViewResolver();

	@Test
	public void shouldResolveByWritingJsonWrappedInFunctionToOutputStream() throws IOException {
		JsonpView viewResult = new JsonpView(map("key", "value"));
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
		assertThat(resp.getBodyAsString(), is("callback({\"key\":\"value\"});"));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
		assertThat(resp.getContentLength(), is(26));
	}

	@Test
	public void shouldResolveJsonElementByWritingJsonToOutputStreamAsJsonElement() throws IOException {
		JsonElement jsonEl = createJsonElement();
		JsonpView viewResult = new JsonpView(jsonEl);
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
		assertThat(resp.getBodyAsString(), is("callback({\"key\":\"value\"});"));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
		assertThat(resp.getContentLength(), is(26));
	}

	@Test
	public void shouldNameCallbackMethodBasedOnCallbackParameter() throws IOException {
		req.withParameter("callback", "abcdef");
		JsonpView viewResult = new JsonpView(map("key", "value"));
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getStatusCode(), is(StatusCode.OK));
		assertThat(resp.getBodyAsString(), is("abcdef({\"key\":\"value\"});"));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
		assertThat(resp.getContentLength(), is(24));
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenFailedToWriteJsonToOutputStream() throws IOException {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Failed to generate JSONP output for object 'string'");

		resp = spy(resp);
		when(resp.getOutputStream()).thenThrow(new RuntimeException("fail"));
		JsonpView viewResult = new JsonpView("string");
		resolver.resolve(req, resp, viewResult);
	}

	@Test
	public void shouldReturnClassNameForToString() {
		assertThat(new JsonpViewResolver().toString(), is("JsonpViewResolver"));
	}

	@Test
	public void shouldSetJavascriptContentType() {
		JsonpView viewResult = new JsonpView(map("key", "value"));
		resolver.resolve(req, resp, viewResult);
		assertThat(resp.getContentType(), is(ContentType.ApplicationJavascript));
	}

	@Test
	public void shouldRespectExtendedViewValues() {
		JsonpView view = new JsonpView(map("key", "value"));
		Cookie cookie = Cookie.build("cookie").withValue("value2").build();
		view.withContentType("content/type")
			.withCharacterEncoding("UTF-16")
			.withHeader("header", "value1")
			.withCookie(cookie);

		resolver.resolve(req, resp, view);
		assertThat(resp.getContentType(), is(nullValue()));
		assertThat(resp.getContentTypeString(), is("content/type"));
		assertThat(resp.getCharacterEncoding(), is("UTF-16"));
		assertThat(resp.getHeader("header"), is("value1"));
		assertThat(resp.getCookies(), hasItem(cookie));
	}

	@Test
	public void shouldAllowAccessToInternalGsonBuilder() {
		assertThat(resolver.getGsonBuilder(), is(notNullValue()));

		GsonBuilder gsonBuilder = new GsonBuilder();
		resolver = new JsonpViewResolver(gsonBuilder);
		assertThat(resolver.getGsonBuilder(), is(sameInstance(gsonBuilder)));
	}

	private JsonElement createJsonElement() {
		Gson gson = new GsonBuilder().create();
		return gson.fromJson("{\"key\":\"value\"}", JsonElement.class);
	}

}
