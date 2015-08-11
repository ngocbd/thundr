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
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.Charsets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class ServletRequestPreservingViewRendererTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private ServletRequestPreservingViewRenderer renderer = new ServletRequestPreservingViewRenderer(viewResolverRegistry, TransformerManager.createWithDefaults());
	private HttpServletRequest req = new MockHttpServletRequest();
	private HttpServletResponse resp = new MockHttpServletResponse();

	private MockRequest request = new MockRequest().withRawRequest(req);
	private MockResponse response = new MockResponse().withRawResponse(resp);

	@Before
	public void before() {
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());

	}

	@Test
	public void shouldRenderAViewToResponse() throws UnsupportedEncodingException {
		renderer.render(request, response, new StringView("contents").withHeader("header", "value"));
		assertThat(response.getBodyAsString(), is("contents"));
		assertThat(response.getHeader("header"), is("value"));
		assertThat(response.getContentType(), is(ContentType.TextPlain));
		assertThat(response.getCharacterEncoding(), is("UTF-8"));
	}

	@Test
	public void shouldThrowViewResolutionExceptionIfNoViewResolver() {
		thrown.expect(ViewResolverNotFoundException.class);
		thrown.expectMessage("No ViewResolver is registered for the view result String - ");

		renderer.render(request, response, "no");
	}

	@Test
	public void shouldUseRequestInThreadLocalIfPresentRestoringInitialState() {
		req.setAttribute("existing", "value");
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request request, Response resp, String viewResult) {
				HttpServletRequest req = request.getRawRequest(HttpServletRequest.class);
				assertThat(req.getAttribute("existing"), is((Object) "value"));
				req.setAttribute("existing", "overwritten");
				assertThat(req.getAttribute("existing"), is((Object) "overwritten"));
				try {
					resp.getOutputStream().write(viewResult.getBytes(Charsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		renderer.render(request, response, "Body");
		assertThat(response.getBodyAsString(), is("Body"));
		assertThat(req.getAttribute("existing"), is((Object) "value"));
	}

	@Test
	public void shouldUseRequestInThreadLocalIfPresentRestoringInitialStateEvenOnException() {
		req.setAttribute("existing", "value");
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request request, Response resp, String viewResult) {
				HttpServletRequest req = request.getRawRequest(HttpServletRequest.class);
				assertThat(req.getAttribute("existing"), is((Object) "value"));
				req.setAttribute("existing", "overwritten");
				assertThat(req.getAttribute("existing"), is((Object) "overwritten"));
				throw new RuntimeException("intentional");
			}
		});
		try {
			renderer.render(request, response, "Body");
			fail("Expected an exception");
		} catch (RuntimeException e) {
			assertThat(req.getAttribute("existing"), is((Object) "value"));
		}
	}
}
