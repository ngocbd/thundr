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
package com.threewks.thundr.route.cors;

import static com.atomicleopard.expressive.Expressive.list;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.transformer.TransformerManager;

public class CorsFilterTest {

	private MockRequest req = new MockRequest(HttpMethod.GET, "/path");
	private MockResponse resp = new MockResponse(TransformerManager.createWithDefaults());

	@Before
	public void before() {
		req.withHeader(Header.Origin, "https://www.origin.com");
	}

	@Test
	public void shouldRetainDomainsFiltersAndWithCredentialsFlag() {
		CorsFilter corsFilter = new CorsFilter(list("origin1", "origin2"), list("X-Header-1", "header-2"), false);
		assertThat(corsFilter.getOrigins(), hasItems("origin1", "origin2"));
		assertThat(corsFilter.getHeaders(), hasItems("x-header-1", "header-2"));
		assertThat(corsFilter.isWithCredentials(), is(false));
	}

	@Test
	public void shouldDefaultIsCredentialsToFalseIfNoOriginsSupplied() {
		assertThat(new CorsFilter().isWithCredentials(), is(false));
		assertThat(new CorsFilter(Collections.<String> emptyList()).isWithCredentials(), is(true));
		assertThat(new CorsFilter(list("origin-1")).isWithCredentials(), is(true));
		assertThat(new CorsFilter(list("origin-1"), list("header")).isWithCredentials(), is(true));
		assertThat(new CorsFilter(null, list("header")).isWithCredentials(), is(false));
		assertThat(new CorsFilter(list("origin-1"), list("header"), true).isWithCredentials(), is(true));
		assertThat(new CorsFilter(list("origin-1"), list("header"), false).isWithCredentials(), is(false));
		assertThat(new CorsFilter(null, list("header"), true).isWithCredentials(), is(true));
	}

	@Test
	public void shouldApplyCorsHeadersForWildcardOrigin() {
		CorsFilter corsFilter = new CorsFilter();
		corsFilter.before(req, resp);
		assertThat(resp.getHeader(Header.AccessControlAllowOrigin), is("*"));
		assertThat(resp.getHeader(Header.AccessControlAllowCredentials), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowMethods), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowHeaders), is(nullValue()));
	}

	@Test
	public void shouldApplyCorsHeadersForWildcardOriginWithRequestMethodAndHeaders() {
		// @formatter:off
		req.withHeader(Header.AccessControlRequestMethod, "POST")
		   .withHeader(Header.AccessControlRequestHeaders, "Accept, X-Custom-Header");
		// @formatter:on
		CorsFilter corsFilter = new CorsFilter();
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.AccessControlAllowOrigin), is("*"));
		assertThat(resp.getHeader(Header.AccessControlAllowCredentials), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowMethods), is("POST"));
		assertThat(resp.getHeader(Header.AccessControlAllowHeaders), is("accept, x-custom-header"));
	}

	@Test
	public void shouldApplyCorsHeadersForNamedOriginWithCredentialsAndSpecificHeaders() {
		req.withHeader(Header.AccessControlRequestMethod, "POST");
		req.withHeader(Header.AccessControlRequestHeaders, "Accept, X-Custom-Header");
		CorsFilter corsFilter = new CorsFilter(list("www.origin.com"), list("X-Custom-Header"));
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.AccessControlAllowOrigin), is("https://www.origin.com"));
		assertThat(resp.getHeader(Header.AccessControlAllowCredentials), is("true"));
		assertThat(resp.getHeader(Header.AccessControlAllowMethods), is("POST"));
		assertThat(resp.getHeader(Header.AccessControlAllowHeaders), is("x-custom-header"));
	}

	@Test
	public void shouldIncludeAnyProtocolForNamedOrigin() {
		req.replaceHeader(Header.Origin, "fake://www.origin.com");
		CorsFilter corsFilter = new CorsFilter(list("www.origin.com"), list("X-Custom-Header"));
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.AccessControlAllowOrigin), is("fake://www.origin.com"));
	}

	@Test
	public void shouldNotIncludeCorsHeadersWhenOriginDoesntMatch() {
		CorsFilter corsFilter = new CorsFilter(list("www.other.com"));
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.AccessControlAllowOrigin), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowCredentials), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowMethods), is(nullValue()));
		assertThat(resp.getHeader(Header.AccessControlAllowHeaders), is(nullValue()));
	}

	@Test
	public void shouldExcludeUnallowedHeaders() {
		CorsFilter corsFilter = new CorsFilter(null, list("X-Header-1", "X-Header-2", "X-Header-3"));
		req.withHeader(Header.AccessControlRequestHeaders, "Accept, X-Disallowed, MadeUp, X-Header-1, X-Header-2");
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.AccessControlAllowHeaders), is("x-header-1, x-header-2"));
	}

	@Test
	public void shouldIncludeVaryHeaderToCauseCorrectCachingPerClientMethodAndHeaders() {
		CorsFilter corsFilter = new CorsFilter();
		corsFilter.before(req, resp);

		assertThat(resp.getHeader(Header.Vary), is("Origin, Access-Control-Request-Method, Access-Control-Request-Headers"));
	}

	@Test
	public void shouldReturnNullForAfterAndException() {
		assertThat(new CorsFilter().after(null, null, null), is(nullValue()));
		assertThat(new CorsFilter().exception(null, null, null), is(nullValue()));
	}
}
