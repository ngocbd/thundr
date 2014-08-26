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
package com.threewks.thundr.view.redirect;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.action.TestAction;
import com.threewks.thundr.action.TestActionResolver;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.view.ViewResolutionException;

public class RouteRedirectViewResolverTest {
	@Rule public ExpectedException thrown = ExpectedException.none();

	private Router router;
	private RouteRedirectViewResolver resolver;
	private HttpServletRequest req = new MockHttpServletRequest();
	private HttpServletResponse resp = mock(HttpServletResponse.class);

	@Before
	public void before() {
		router = new Router();
		router.addResolver(TestAction.class, new TestActionResolver());
		router.add(HttpMethod.GET, "/route/1", "route1", new TestAction("route1"));
		router.add(HttpMethod.GET, "/route/{var}", "route2", new TestAction("route2"));
		router.add(HttpMethod.GET, "/route/{var}/{var2}", "route3", new TestAction("route3"));

		resolver = new RouteRedirectViewResolver(router);
	}

	@Test
	public void shouldRedirectToNamedRoute() throws IOException {
		RouteRedirectView viewResult = new RouteRedirectView("route1");
		resolver.resolve(req, resp, viewResult);
		verify(resp).sendRedirect("/route/1");
	}

	@Test
	public void shouldRedirectToNamedRouteSubstitutingVariables() throws IOException {
		RouteRedirectView viewResult = new RouteRedirectView("route2", Expressive.<String, Object> map("var", "expected"));
		resolver.resolve(req, resp, viewResult);
		verify(resp).sendRedirect("/route/expected");
	}
	@Test
	public void shouldRedirectToNamedRouteWithQueryParameters() throws IOException {
		RouteRedirectView viewResult = new RouteRedirectView("route2", Expressive.<String, Object> map("var", "expected"), Expressive.<String, Object> map("q1", "v1", "q2", "v2"));
		resolver.resolve(req, resp, viewResult);
		verify(resp).sendRedirect("/route/expected?q2=v2&q1=v1");
	}

	@Test
	public void shouldOnlyIncludeQueryStringIfNecessary() throws IOException {
		RouteRedirectView viewResult = new RouteRedirectView("route2", Expressive.<String, Object> map("var", "expected"), Expressive.<String, Object> map());
		resolver.resolve(req, resp, viewResult);
		verify(resp).sendRedirect("/route/expected");
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenRedirectFails() throws IOException {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Failed to redirect to route 'route1' (/route/1): BOOM");

		doThrow(new IOException("BOOM")).when(resp).sendRedirect(anyString());

		RouteRedirectView viewResult = new RouteRedirectView("route1");
		resolver.resolve(req, resp, viewResult);
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenRedirectToNonexistantRoute() throws IOException {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Cannot redirect to the route named 'fake': no route with this name exists");

		RouteRedirectView viewResult = new RouteRedirectView("fake");
		resolver.resolve(req, resp, viewResult);
	}

	@Test
	public void shouldThrowViewResolutionExceptionWhenPathVariablesAreNotAvailable() throws IOException {
		thrown.expect(ViewResolutionException.class);
		thrown.expectMessage("Failed to redirect to route '/route/{var}': Cannot generate a reverse route for /route/{var} - no value(s) supplied for the path variables var");

		RouteRedirectView viewResult = new RouteRedirectView("route2");
		resolver.resolve(req, resp, viewResult);
	}

	@Test
	public void shouldReturnClassNameForToString() {
		assertThat(resolver.toString(), is("RouteRedirectViewResolver"));
	}
}
