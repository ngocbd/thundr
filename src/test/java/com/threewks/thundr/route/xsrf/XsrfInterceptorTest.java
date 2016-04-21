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
package com.threewks.thundr.route.xsrf;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;

public class XsrfInterceptorTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private MockRequest req = new MockRequest();
	private MockResponse resp = new MockResponse();
	private XsrfInterceptor interceptor = new XsrfInterceptor();

	@Test
	public void shouldAddXsrfTokenIfNotPresentOnGet() {
		req.withMethod(HttpMethod.GET);
		interceptor.before(null, req, resp);
		assertThat(resp.getCookie("XSRF-TOKEN").getValue(), is(notNullValue()));
	}

	@Test
	public void shouldAddXsrfTokenIfNotPresentOnHead() {
		req.withMethod(HttpMethod.HEAD);
		interceptor.before(null, req, resp);
		assertThat(resp.getCookie("XSRF-TOKEN").getValue(), is(notNullValue()));
	}

	@Test
	public void shouldAddXsrfTokenIfNotPresentOnOptions() {
		req.withMethod(HttpMethod.OPTIONS);
		interceptor.before(null, req, resp);
		assertThat(resp.getCookie("XSRF-TOKEN").getValue(), is(notNullValue()));
	}

	@Test
	public void shouldFailWithMissingXsrfTokenOnPost() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on POST /path - XSRF data missing, perform a GET first.");

		req.withMethod(HttpMethod.POST);
		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMissingXsrfTokenOnPut() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on PUT /path - XSRF data missing, perform a GET first.");

		req.withMethod(HttpMethod.PUT);
		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMissingXsrfTokenOnDelete() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on DELETE /path - XSRF data missing, perform a GET first.");

		req.withMethod(HttpMethod.DELETE);
		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMismatchedXsrfTokenOnPost() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on POST /path - invalid 'X-XSRF-TOKEN' XSRF token.");

		// @formatter:off
		req = req
				.withMethod(HttpMethod.POST)
				.withCookie("XSRF-TOKEN", "value")
				.withHeader(Header.XXsrfToken, "other-value");
		// @formatter:on

		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMismatchedXsrfTokenOnPut() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on PUT /path - invalid 'X-XSRF-TOKEN' XSRF token.");

		// @formatter:off
		req = req
				.withMethod(HttpMethod.PUT)
				.withCookie("XSRF-TOKEN", "value")
				.withHeader(Header.XXsrfToken, "other-value");
		// @formatter:on

		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMismatchedXsrfTokenOnDelete() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on DELETE /path - invalid 'X-XSRF-TOKEN' XSRF token.");

		// @formatter:off
		req = req
				.withMethod(HttpMethod.DELETE)
				.withCookie("XSRF-TOKEN", "value")
				.withHeader(Header.XXsrfToken, "other-value");
		// @formatter:on

		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldFailWithMissingCookieOnPost() {
		thrown.expect(HttpStatusException.class);
		thrown.expectMessage("XSRF rejected request on POST /path - XSRF data missing, perform a GET first.");

		// @formatter:off
		req = req
				.withMethod(HttpMethod.POST)
				.withHeader(Header.XXsrfToken, "value");
		// @formatter:on

		interceptor.before(null, req, resp);
	}

	@Test
	public void shouldPassWithMatchingXsrfTokensOnPost() {
		// @formatter:off
		req = req
				.withMethod(HttpMethod.POST)
				.withCookie("XSRF-TOKEN", "value")
				.withHeader(Header.XXsrfToken, "value");
		// @formatter:on

		assertThat(interceptor.before(null, req, resp), is(nullValue()));
		assertThat(req.getData("xsrf"), is(notNullValue()));
	}

	@Test
	public void shouldPassWithMatchingXsrfTokensOnPostParameter() {
		// @formatter:off
		req = req
				.withMethod(HttpMethod.POST)
				.withCookie("XSRF-TOKEN", "value")
				.withParameter("XSRF-TOKEN", "value");
		// @formatter:on

		assertThat(interceptor.before(null, req, resp), is(nullValue()));
		assertThat(req.getData("xsrf"), is(notNullValue()));
	}

	@Test
	public void shouldDoNothingOnGetIfXsrfTokenPresent() {
		// @formatter:off
		req.withMethod(HttpMethod.GET)
			.withCookie("XSRF-TOKEN", "value");
		// @formatter:on
		assertThat(interceptor.before(null, req, resp), is(nullValue()));
		assertThat(resp.getCookie("XSRF-TOKEN"), is(nullValue()));
		assertThat(req.getData("xsrf"), is((Object) "value"));
	}

}
