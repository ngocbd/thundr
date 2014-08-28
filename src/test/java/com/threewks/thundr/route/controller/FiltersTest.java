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
package com.threewks.thundr.route.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.InOrder;

import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.controller.Filter;
import com.threewks.thundr.route.controller.FilterRegistry;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class FiltersTest {
	private FilterRegistry filters = new FilterRegistry();
	private Filter filter1 = mock(Filter.class);
	private Filter filter2 = mock(Filter.class);
	private Filter filter3 = mock(Filter.class);
	private MockHttpServletRequest req = new MockHttpServletRequest();
	private MockHttpServletResponse resp = new MockHttpServletResponse();
	private Exception e = new Exception();

	@Test
	public void shouldRetainGivenFilterAndMapping() {
		filters.add("/**", filter1);
		assertThat(filters.has("/**", filter1), is(true));
	}

	@Test
	public void shouldAllowMultipleFiltersPerPath() {
		filters.add("/**", filter1);
		filters.add("/**", filter2);
		assertThat(filters.has("/**", filter1), is(true));
		assertThat(filters.has("/**", filter2), is(true));
	}

	@Test
	public void shouldAllowAFilterToBePlacedOnManyPaths() {
		filters.add("/a/**", filter1);
		filters.add("/b/**", filter1);
		assertThat(filters.has("/**", filter1), is(false));
		assertThat(filters.has("/a/**", filter1), is(true));
		assertThat(filters.has("/b/**", filter1), is(true));
	}

	@Test
	public void shouldAllowRemovalOfFilterOnPath() {
		filters.add("/a/**", filter1);
		filters.add("/b/**", filter1);

		assertThat(filters.has("/a/**", filter1), is(true));
		assertThat(filters.has("/b/**", filter1), is(true));

		filters.remove("/a/**", filter1);

		assertThat(filters.has("/a/**", filter1), is(false));
		assertThat(filters.has("/b/**", filter1), is(true));
	}

	@Test
	public void shouldNotFailRemovingFilterthatWasntAdded() {
		assertThat(filters.has("/a/**", filter1), is(false));

		filters.remove("/a/**", filter1);

		assertThat(filters.has("/a/**", filter1), is(false));
	}

	@Test
	public void shouldAllowRemovalOfFilterFromAllPaths() {
		filters.add("/a/**", filter1);
		filters.add("/b/**", filter1);

		assertThat(filters.has("/a/**", filter1), is(true));
		assertThat(filters.has("/b/**", filter1), is(true));

		filters.remove(filter1);

		assertThat(filters.has("/a/**", filter1), is(false));
		assertThat(filters.has("/b/**", filter1), is(false));
	}

	@Test
	public void shouldRunBeforeOnAllFiltersMatchingPath() {
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);
		filters.add("/other/**", filter3);

		req.url("/sub/path/request");
		Object result = filters.before(HttpMethod.GET, req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter1).before(HttpMethod.GET, req, resp);
		inOrder.verify(filter2).before(HttpMethod.GET, req, resp);
		verify(filter3, never()).before(HttpMethod.GET, req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentBeforeFiltersWhenFilterReturnsNonNullValue() {
		when(filter1.before(HttpMethod.GET, req, resp)).thenReturn("Result");
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);

		req.url("/sub/path/request");
		Object result = filters.before(HttpMethod.GET, req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter1).before(HttpMethod.GET, req, resp);
		verify(filter2, never()).before(HttpMethod.GET, req, resp);
	}

	@Test
	public void shouldRunAfterOnAllFiltersMatchingPath() {
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);
		filters.add("/other/**", filter3);

		req.url("/sub/path/request");
		Object result = filters.after(HttpMethod.GET, "View", req, resp);
		assertThat(result, is(nullValue()));

		verify(filter1).after(HttpMethod.GET, "View", req, resp);
		verify(filter2).after(HttpMethod.GET, "View", req, resp);
		verify(filter3, never()).after(HttpMethod.GET, "View", req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentAfterFiltersWhenFilterReturnsNonNullValue() {
		when(filter2.after(HttpMethod.GET, "View", req, resp)).thenReturn("Result");
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);

		req.url("/sub/path/request");
		Object result = filters.after(HttpMethod.GET, "View", req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter2).after(HttpMethod.GET, "View", req, resp);
		verify(filter1, never()).after(HttpMethod.GET, "View", req, resp);
	}

	@Test
	public void shouldRunExcpetionOnAllFiltersMatchingPath() {
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);
		filters.add("/other/**", filter3);

		req.url("/sub/path/request");
		Object result = filters.exception(HttpMethod.GET, e, req, resp);
		assertThat(result, is(nullValue()));

		verify(filter1).exception(HttpMethod.GET, e, req, resp);
		verify(filter2).exception(HttpMethod.GET, e, req, resp);
		verify(filter3, never()).exception(HttpMethod.GET, e, req, resp);
	}

	@Test
	public void shouldReturnViewAndSkipSubsequentExceptionFiltersWhenFilterReturnsNonNullValue() {
		when(filter2.exception(HttpMethod.GET, e, req, resp)).thenReturn("Result");
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);

		req.url("/sub/path/request");
		Object result = filters.exception(HttpMethod.GET, e, req, resp);
		assertThat(result, is((Object) "Result"));

		verify(filter2).exception(HttpMethod.GET, e, req, resp);
		verify(filter1, never()).exception(HttpMethod.GET, e, req, resp);
	}

	@Test
	public void shouldRunAfterFiltersInReverseOrder() {
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);

		req.url("/sub/path/request");
		Object result = filters.after(HttpMethod.GET, "View", req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter2).after(HttpMethod.GET, "View", req, resp);
		inOrder.verify(filter1).after(HttpMethod.GET, "View", req, resp);

	}

	@Test
	public void shouldRunExceptionFiltersInReverseOrder() {
		filters.add("/**", filter1);
		filters.add("/sub/**", filter2);

		req.url("/sub/path/request");
		Object result = filters.exception(HttpMethod.GET, e, req, resp);
		assertThat(result, is(nullValue()));

		InOrder inOrder = inOrder(filter1, filter2);
		inOrder.verify(filter2).exception(HttpMethod.GET, e, req, resp);
		inOrder.verify(filter1).exception(HttpMethod.GET, e, req, resp);
	}
}
