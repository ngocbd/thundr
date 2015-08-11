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

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.request.InMemoryResponse;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class BasicViewRendererTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private ViewResolverRegistry viewResolverRegistry = new ViewResolverRegistry();
	private BasicViewRenderer renderer = new BasicViewRenderer(viewResolverRegistry);
	private Request request = new MockRequest();
	private InMemoryResponse response = new InMemoryResponse(TransformerManager.createWithDefaults());

	@Before
	public void before() {
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());
	}

	@Test
	public void shouldRenderAViewMakingOutputAndHeadersAvailable() throws UnsupportedEncodingException {
		renderer.render(request, response, new StringView("contents").withHeader("header", "value"));
		assertThat(response.getBodyAsString(), is("contents"));
		assertThat(response.getBodyAsBytes(), is("contents".getBytes("UTF-8")));
		assertThat(response.getContentType(), is(ContentType.TextPlain));
		assertThat(response.getCharacterEncoding(), is("UTF-8"));
		assertThat(response.getHeader("header"), is("value"));
		assertThat(response.getHeader("HEADER"), is(nullValue()));
	}

	@Test
	public void shouldThrowViewResolutionExceptionIfNoViewResolver() {
		thrown.expect(ViewResolverNotFoundException.class);
		thrown.expectMessage("No ViewResolver is registered for the view result String - ");

		renderer.render(request, response, "no");
	}
}
