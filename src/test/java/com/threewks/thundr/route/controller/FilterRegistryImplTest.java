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
package com.threewks.thundr.route.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.InOrder;

import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.transformer.TransformerManager;

public class FilterRegistryImplTest {
	private FilterRegistry registry = new FilterRegistryImpl();
	private Filter filter1 = mock(Filter.class);
	private Filter filter2 = mock(Filter.class);
	private Filter filter3 = mock(Filter.class);
	private MockRequest req = new MockRequest(HttpMethod.GET, "/path");
	private MockResponse resp = new MockResponse(TransformerManager.createWithDefaults());
	private Exception e = new Exception();

	@Test
	public void shouldRetainGivenFilterAndMapping() {
		registry.add(filter1, "/**");
		assertThat(registry.has(filter1, "/**"), is(true));
	}

	@Test
	public void shouldAllowMultipleFiltersPerPath() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/**");
		assertThat(registry.has(filter1, "/**"), is(true));
		assertThat(registry.has(filter2, "/**"), is(true));
	}

	@Test
	public void shouldAllowAFilterToBePlacedOnManyPaths() {
		registry.add(filter1, "/a/**");
		registry.add(filter1, "/b/**");
		assertThat(registry.has(filter1, "/**"), is(false));
		assertThat(registry.has(filter1, "/a/**"), is(true));
		assertThat(registry.has(filter1, "/b/**"), is(true));
	}

	@Test
	public void shouldAllowRemovalOfFilterOnPath() {
		registry.add(filter1, "/a/**");
		registry.add(filter1, "/b/**");

		assertThat(registry.has(filter1, "/a/**"), is(true));
		assertThat(registry.has(filter1, "/b/**"), is(true));

		registry.remove(filter1, "/a/**");

		assertThat(registry.has(filter1, "/a/**"), is(false));
		assertThat(registry.has(filter1, "/b/**"), is(true));
	}

	@Test
	public void shouldNotFailRemovingFilterthatWasntAdded() {
		assertThat(registry.has(filter1, "/a/**"), is(false));

		registry.remove(filter1, "/a/**");

		assertThat(registry.has(filter1, "/a/**"), is(false));
	}

	@Test
	public void shouldAllowRemovalOfFilterFromAllPaths() {
		registry.add(filter1, "/a/**");
		registry.add(filter1, "/b/**");

		assertThat(registry.has(filter1, "/a/**"), is(true));
		assertThat(registry.has(filter1, "/b/**"), is(true));

		registry.remove(filter1);

		assertThat(registry.has(filter1, "/a/**"), is(false));
		assertThat(registry.has(filter1, "/b/**"), is(false));
	}

	@Test
	public void shouldRunBeforeOnAllFiltersMatchingPath() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");
		registry.add(filter3, "/other/**");

		req.withUrl("/sub/path/request");
		Object result = registry.before(req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter1).before(req, resp);
		inOrder.verify(filter2).before(req, resp);
		verify(filter3, never()).before(req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentBeforeFiltersWhenFilterReturnsNonNullValue() {
		when(filter1.before(req, resp)).thenReturn("Result");
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");

		req.withUrl("/sub/path/request");
		Object result = registry.before(req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter1).before(req, resp);
		verify(filter2, never()).before(req, resp);
	}

	@Test
	public void shouldRunAfterOnAllFiltersMatchingPath() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");
		registry.add(filter3, "/other/**");

		req.withUrl("/sub/path/request");
		Object result = registry.after("View", req, resp);
		assertThat(result, is(nullValue()));

		verify(filter1).after("View", req, resp);
		verify(filter2).after("View", req, resp);
		verify(filter3, never()).after("View", req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentAfterFiltersWhenFilterReturnsNonNullValue() {
		when(filter2.after("View", req, resp)).thenReturn("Result");
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");

		req.withUrl("/sub/path/request");
		Object result = registry.after("View", req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter2).after("View", req, resp);
		verify(filter1, never()).after("View", req, resp);
	}

	@Test
	public void shouldRunExcpetionOnAllFiltersMatchingPath() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");
		registry.add(filter3, "/other/**");

		req.withUrl("/sub/path/request");
		Object result = registry.exception(e, req, resp);
		assertThat(result, is(nullValue()));

		verify(filter1).exception(e, req, resp);
		verify(filter2).exception(e, req, resp);
		verify(filter3, never()).exception(e, req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentExceptionFiltersWhenFilterReturnsNonNullValue() {
		when(filter2.exception(e, req, resp)).thenReturn("Result");
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");

		req.withUrl("/sub/path/request");
		Object result = registry.exception(e, req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter2).exception(e, req, resp);
		verify(filter1, never()).exception(e, req, resp);
	}

	@Test
	public void shouldRunAfterFiltersInReverseOrder() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");

		req.withUrl("/sub/path/request");
		Object result = registry.after("View", req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter2).after("View", req, resp);
		inOrder.verify(filter1).after("View", req, resp);

	}

	@Test
	public void shouldRunExceptionFiltersInReverseOrder() {
		registry.add(filter1, "/**");
		registry.add(filter2, "/sub/**");

		req.withUrl("/sub/path/request");
		Object result = registry.exception(e, req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter2).exception(e, req, resp);
		inOrder.verify(filter1).exception(e, req, resp);
	}

	@Test
	public void shouldRetainOrderingOfFilterBasedOnAdditionOrder() {
		// This represents a consistent failure based on a previous implementation (which was a hashmap)
		registry.add(filter1, "/**");
		registry.add(filter2, "/*");

		req.withUrl("/path");
		Object result = registry.before(req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter1).before(req, resp);
		inOrder.verify(filter2).before(req, resp);

	}
}
