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
package com.threewks.thundr.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.Cookies.CookieBuilder;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class BaseViewTest {

	private TestView view;

	@Before
	public void before() {
		view = new TestView();
	}

	@Test
	public void shouldRetainStatusCodeFromEnum() {
		assertThat(view.withStatusCode(StatusCode.Accepted), is(view));
		assertThat(view.getStatusCode(), is(202));
	}

	@Test
	public void shouldRetainStatusCode() {
		assertThat(view.withStatusCode(201), is(view));
		assertThat(view.getStatusCode(), is(201));
	}

	@Test
	public void shouldRetainHeader() {
		assertThat(view.withHeader("header", "value"), is(view));
		assertThat(view.getHeader("header"), is("value"));
		assertThat(view.getHeaders(), hasEntry("header", "value"));
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
		MockHttpServletResponse resp = new MockHttpServletResponse();
		assertThat(resp.getContentType(), is(nullValue()));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));

		BaseView.applyToResponse(new TestView(), resp);

		assertThat(resp.getContentType(), is(nullValue()));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
		assertThat(resp.isCommitted(), is(false));
		assertThat(resp.status(), is(-1));
	}

	@Test
	public void shouldApplyViewValuesToResponse() {
		MockHttpServletResponse resp = new MockHttpServletResponse();

		TestView view = new TestView().withCharacterEncoding("UTF-16").withStatusCode(StatusCode.AlreadyReported).withContentType("text/html");
		view.withHeader("header", "value").withCookie("cookie", "value2");
		BaseView.applyToResponse(view, resp);

		assertThat(resp.getContentType(), is("text/html"));
		assertThat(resp.getCharacterEncoding(), is("UTF-16"));
		assertThat(resp.isCommitted(), is(false));
		assertThat(resp.status(), is(208));
	}

	@Test
	public void shouldIncludeModelInRequest() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("attr1", "value1");
		model.put("attr2", 2);
		BaseView.includeModelInRequest(req, model);
		assertThat(req.getAttribute("attr1"), is((Object) "value1"));
		assertThat(req.getAttribute("attr2"), is((Object) 2));
	}

	@Test
	public void shouldIncludeModelInRequestReplacingExitingValues() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.attribute("attr1", "old");
		req.attribute("attr2", "old");

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("attr1", "value1");
		model.put("attr2", 2);
		BaseView.includeModelInRequest(req, model);
		assertThat(req.getAttribute("attr1"), is((Object) "value1"));
		assertThat(req.getAttribute("attr2"), is((Object) 2));
	}

	private static class TestView extends BaseView<TestView> {

	}
}
