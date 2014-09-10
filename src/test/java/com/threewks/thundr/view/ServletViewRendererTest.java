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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.RequestThreadLocal;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class ServletViewRendererTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private ServletViewRenderer renderer = new ServletViewRenderer(viewResolverRegistry, true);
	private HttpServletRequest req = new MockHttpServletRequest();
	private MockHttpServletResponse resp = new MockHttpServletResponse();

	@Before
	public void before() {
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());
		RequestThreadLocal.set(req, resp);

	}

	@After
	public void after() {
		RequestThreadLocal.clear();
	}

	@Test
	public void shouldRenderAViewToResponse() throws UnsupportedEncodingException {
		renderer.render(new StringView("contents").withHeader("header", "value"));
		assertThat(resp.content(), is("contents"));
		assertThat(resp.header("header"), is((Object) "value"));
		assertThat(resp.getContentType(), is("text/plain"));
		assertThat(resp.getCharacterEncoding(), is("UTF-8"));
	}

	@Test
	public void shouldThrowViewResolutionExceptionIfNoViewResolver() {
		thrown.expect(ViewResolverNotFoundException.class);
		thrown.expectMessage("No ViewResolver is registered for the view result String - ");

		renderer.render("no");
	}

	@Test
	public void shouldNotThrowViewResolutionExceptionIfNoViewResolver() {
		renderer = new ServletViewRenderer(viewResolverRegistry, false);

		renderer.render("no");
		assertThat(resp.isCommitted(), is(false));
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenTryingToReuseABasicViewRenderer() {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("This ServletViewRenderer has already been used to render a view, it cannot be reused. Create a new one");

		renderer.render(new StringView("contents"));
		renderer.render(new StringView("contents"));
	}

}
