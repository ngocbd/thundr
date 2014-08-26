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
package com.threewks.thundr.route;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.route.redirect.Redirect;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class RouterTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private Router router;

	private MockHttpServletRequest req = new MockHttpServletRequest();
	private MockHttpServletResponse resp = new MockHttpServletResponse();

	@Before
	public void before() {
		router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
	}

	@Test
	public void shouldReturnPreviouslyAddedActionResolver() {
		RouteResolver<TestResolve> actionResolver = router.getResolver(TestResolve.class);
		assertThat(actionResolver, is(notNullValue()));
		assertThat(actionResolver instanceof TestRouteResolver, is(true));

		// This is unregistered
		assertThat(router.getResolver(Redirect.class), is(nullValue()));
	}

	@Test
	public void shouldHaveRouteAfterAddingRoutes() {
		Router router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
		router.add(HttpMethod.GET, "/path/*.jpg", null, new TestResolve(null));

		assertThat(router.findMatchingRoute("/path/image.jpg", HttpMethod.GET), is(notNullValue()));
		assertThat(router.findMatchingRoute("/path/image.jpg", HttpMethod.POST), is(nullValue()));
		assertThat(router.findMatchingRoute("/path/image.jpeg", HttpMethod.GET), is(nullValue()));
	}

	@Test
	public void shouldReturnTrueIfNoRoutesHaveBeenAdded() {
		Router router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
		assertThat(router.isEmpty(), is(true));
		router.add(HttpMethod.GET, "/", null, (TestResolve) null);
		assertThat(router.isEmpty(), is(false));
	}

	@Test
	public void shouldThrowRouteExceptionWhenAddingRouteWithNameThatIsAlreadyTaken() {
		thrown.expect(RouteException.class);
		thrown.expectMessage("Unable to add the route 'GET /route2/' with the name 'name' - the route 'GET /route/' has already been registered with this name");
		router.add(HttpMethod.GET, "/route/", "name", new TestResolve("action"));
		router.add(HttpMethod.GET, "/route2/", "name", new TestResolve("action"));
	}

	@Test
	public void shouldThrowRouteExceptionWhenAddingRouteWithSamePattern() {
		thrown.expect(RouteException.class);
		thrown.expectMessage("Unable to add the route 'GET /route/{var2}' - the route 'GET /route/{var}' already exists which matches the same pattern");
		router.add(HttpMethod.GET, "/route/{var}", null, new TestResolve("action"));
		router.add(HttpMethod.GET, "/route/{var2}", null, new TestResolve("action"));
	}

	@Test
	public void shouldInvokeRouteByReturningTheResultOfTheAction() {
		router.add(HttpMethod.GET, "/route", null, new TestResolve("actionName"));
		Object result = router.invoke("/route", HttpMethod.GET, req, resp);
		assertThat(result, is(notNullValue()));
		assertThat(result instanceof TestResolve, is(true));
	}

	@Test
	public void shouldThrowRouteNotFoundExceptionWhenRouteIsntMatched() {
		thrown.expect(RouteNotFoundException.class);
		thrown.expectMessage("No route matching the request GET /route\n");
		router.invoke("/route", HttpMethod.GET, req, resp);
	}

	@Test
	public void shouldReturnNamedRouteOrNull() {
		router.add(HttpMethod.GET, "/route", "name", new TestResolve("actionName"));

		assertThat(router.getRoute("name").getRoute(), is("/route"));
		assertThat(router.getRoute("otherName"), is(nullValue()));
		assertThat(router.getRoute(null), is(nullValue()));
		assertThat(router.getRoute(""), is(nullValue()));
	}

	@Test
	public void shouldListRoutes() {
		Router router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
		router.add(HttpMethod.GET, "/path/*.jpg", null, new TestResolve("action1"));
		router.add(HttpMethod.PUT, "/path/*.jpg", "PUT", new TestResolve("action2"));

		// @formatter:off
		String expected = 
			"GET     /path/*.jpg                                                       : action1\n" +
			"PUT     /path/*.jpg                                                  (PUT): action2\n";
		// @formatter:on
		assertThat(router.listRoutes(), is(expected));

	}

	private static class TestResolve implements RouteResult {

		private String actionName;

		public TestResolve(String actionName) {
			this.actionName = actionName;
		}

		@Override
		public String toString() {
			return actionName;
		}

	}

	private static class TestRouteResolver implements RouteResolver<TestResolve> {
		@Override
		public TestResolve resolve(TestResolve action, HttpMethod routeType, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) throws RouteResolverException {
			return action;
		}

		@Override
		public TestResolve createActionIfPossible(String actionName) {
			return new TestResolve(actionName);
		}

		@Override
		public void initialise(TestResolve action) {
		}
	}
}
