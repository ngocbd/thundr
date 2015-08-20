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
package com.threewks.thundr.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.http.Cookie.CookieBuilder;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.mock.MockResponse;

public class BaseViewTest {

	private TestView view;

	@Before
	public void before() {
		view = new TestView();
	}

	@Test
	public void shouldRetainStatusCode() {
		assertThat(view.withStatusCode(StatusCode.Accepted), is(view));
		assertThat(view.getStatusCode(), is(StatusCode.Accepted));
	}

	@Test
	public void shouldRetainHeader() {
		assertThat(view.withHeader("header", "value"), is(view));
		assertThat(view.getHeader("header"), is((Object) "value"));
		assertThat(view.getHeaders(), hasEntry((Object) "header", (Object) "value"));
	}

	@Test
	public void shouldRetainCookieFromString() {
		assertThat(view.withCookie("name", "value"), is(view));
		assertThat(view.getCookie("name").getValue(), is("value"));
		assertThat(view.getCookies(), hasKey("name"));
		assertThat(view.getCookies().get("name").getValue(), is("value"));
	}

	@Test
	public void shouldRetainCookie() {
		Cookie cookie = new CookieBuilder("name").withValue("value").build();
		assertThat(view.withCookie(cookie), is(view));
		assertThat(view.getCookie("name").getValue(), is("value"));
		assertThat(view.getCookies(), hasKey("name"));
		assertThat(view.getCookies().get("name").getValue(), is("value"));
	}

	@Test
	public void shouldRetainContentType() {
		assertThat(view.getContentType(), is(nullValue()));
		assertThat(view.withContentType("text/plain"), is(view));
		assertThat(view.getContentType(), is("text/plain"));

	}

	@Test
	public void shouldCharacterEncoding() {
		assertThat(view.getCharacterEncoding(), is(nullValue()));
		assertThat(view.withCharacterEncoding("UTF-8"), is(view));
		assertThat(view.getCharacterEncoding(), is("UTF-8"));
	}

	@Test
	public void shouldApplyDefaultViewValuesToResponse() {
		MockResponse resp = new MockResponse();

		// @formatter:off
		resp.withContentType((String)null)
			.withCharacterEncoding("UTF-8")
			.withStatusCode(null);
		// @formatter:on

		BaseView.applyToResponse(new TestView(), resp);

		assertThat(resp.getContentType(), is(ContentType.Null));
		assertThat(resp.getContentTypeString(), is(nullValue()));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
		assertThat(resp.isCommitted(), is(false));
		assertThat(resp.getStatusCode(), is(nullValue()));
	}

	@Test
	public void shouldApplyViewValuesToResponse() {
		MockResponse resp = new MockResponse();

		// @formatter:off
		TestView view = new TestView()
			.withCharacterEncoding("UTF-16")
			.withStatusCode(StatusCode.AlreadyReported)
			.withContentType("text/html")
			.withHeader("header", "value")
			.withCookie("cookie", "value2");
		// @formatter:on

		BaseView.applyToResponse(view, resp);

		assertThat(resp.getContentTypeString(), is("text/html"));
		assertThat(resp.getContentType(), is(ContentType.TextHtml));
		assertThat(resp.getCharacterEncoding(), is("UTF-16"));
		assertThat(resp.isCommitted(), is(false));
		assertThat(resp.getStatusCode(), is(StatusCode.AlreadyReported));
	}

	/*
	 * TODO - v3 - This code got moved - need to identify where and move these tests
	 * 
	 * 
	 * @Test
	 * public void shouldIncludeModelInRequest() {
	 * MockRequest req = new MockRequest(HttpMethod.GET, "/path");
	 * Map<String, Object> model = new HashMap<String, Object>();
	 * model.put("attr1", "value1");
	 * model.put("attr2", 2);
	 * BaseView.includeModelInRequest(req, model);
	 * assertThat(req.getAttribute("attr1"), is((Object) "value1"));
	 * assertThat(req.getAttribute("attr2"), is((Object) 2));
	 * }
	 * 
	 * @Test
	 * public void shouldIncludeModelInRequestReplacingExitingValues() {
	 * MockHttpServletRequest req = new MockHttpServletRequest();
	 * req.attribute("attr1", "old");
	 * req.attribute("attr2", "old");
	 * 
	 * Map<String, Object> model = new HashMap<String, Object>();
	 * model.put("attr1", "value1");
	 * model.put("attr2", 2);
	 * BaseView.includeModelInRequest(req, model);
	 * assertThat(req.getAttribute("attr1"), is((Object) "value1"));
	 * assertThat(req.getAttribute("attr2"), is((Object) 2));
	 * }
	 */

	private static class TestView extends BaseView<TestView> {

	}
}
