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
package com.threewks.thundr.route.redirect;

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.RouteResolverException;

public class RedirectActionResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private RedirectRouteResolver resolver = new RedirectRouteResolver();
	private Route route;
	
	@Before
	public void before() {
		route = mock(Route.class);
	}

	@Test
	public void shouldSendRedirectToClient() throws IOException {
		Redirect action = new Redirect("/redirect/{to}");
		HttpMethod method = HttpMethod.POST;
		Map<String, String> pathVars = map("to", "new");
		when(route.getPathVars(anyString())).thenReturn(pathVars);
		Request req = new MockRequest(method, "/request", route);
		MockResponse resp = new MockResponse();
		resolver.resolve(action, req, resp);

		assertThat(resp.getStatusCode(), is(StatusCode.Found));
		assertThat(resp.getHeader("Location"), is("/redirect/new"));
	}

	@Test
	public void shouldThrowActionExceptionWhenRedirectFails() throws IOException {
		thrown.expect(RouteResolverException.class);
		thrown.expectMessage("Failed to redirect /requested/path to /redirect/new");

		Redirect action = new Redirect("/redirect/{to}");
		HttpMethod method = HttpMethod.POST;
		Map<String, String> pathVars = map("to", "new");
		when(route.getPathVars(anyString())).thenReturn(pathVars);
		Request req = new MockRequest(method, "/requested/path", route);
		Response resp = mock(Response.class);
		when(resp.withStatusCode(Mockito.any(StatusCode.class))).thenThrow(new RuntimeException("Intentional"));

		resolver.resolve(action, req, resp);
	}
}
