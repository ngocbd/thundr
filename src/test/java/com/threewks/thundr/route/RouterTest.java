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
package com.threewks.thundr.route;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.route.redirect.Redirect;

public class RouterTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private Router router;
	private Request req = new MockRequest(HttpMethod.GET, "/path");
	private Response resp = mock(Response.class);

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
		router.add(HttpMethod.GET, "/path/*.jpg", new TestResolve(null), null);

		assertThat(router.findMatchingRoute(HttpMethod.GET, "/path/image.jpg"), is(notNullValue()));
		assertThat(router.findMatchingRoute(HttpMethod.POST, "/path/image.jpg"), is(nullValue()));
		assertThat(router.findMatchingRoute(HttpMethod.GET, "/path/image.jpeg"), is(nullValue()));
	}

	@Test
	public void shouldReturnTrueIfNoRoutesHaveBeenAdded() {
		Router router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
		assertThat(router.isEmpty(), is(true));
		router.add(HttpMethod.GET, "/", (TestResolve) null, null);
		assertThat(router.isEmpty(), is(false));
	}

	@Test
	public void shouldThrowRouteExceptionWhenAddingRouteWithNameThatIsAlreadyTaken() {
		thrown.expect(RouteException.class);
		thrown.expectMessage("Unable to add the route 'GET /route2/' with the name 'name' - the route 'GET /route/' has already been registered with this name");
		router.add(HttpMethod.GET, "/route/", new TestResolve("action"), "name");
		router.add(HttpMethod.GET, "/route2/", new TestResolve("action"), "name");
	}

	@Test
	public void shouldThrowRouteExceptionWhenAddingRouteWithSamePattern() {
		thrown.expect(RouteException.class);
		thrown.expectMessage("Unable to add the route 'GET /route/{var2}' - the route 'GET /route/{var}' already exists which matches the same pattern");
		router.add(HttpMethod.GET, "/route/{var}", new TestResolve("action"), null);
		router.add(HttpMethod.GET, "/route/{var2}", new TestResolve("action"), null);
	}

	@Test
	public void shouldInvokeRouteByReturningTheResultOfTheAction() {
		router.add(HttpMethod.GET, "/route", new TestResolve("actionName"), null);
		Route route = router.findMatchingRoute(HttpMethod.GET, "/route");
		req = new MockRequest(HttpMethod.GET, "/route", route);

		Object result = router.resolve(req, resp);
		assertThat(result, is(notNullValue()));
		assertThat(result instanceof TestResolve, is(true));
	}

	@Test
	public void shouldThrowRouteNotFoundExceptionWhenRouteIsntMatched() {
		thrown.expect(RouteNotFoundException.class);
		thrown.expectMessage("No route matching the request GET /path\n");
		router.resolve(req, resp);
	}

	@Test
	public void shouldReturnNamedRouteOrNull() {
		router.add(HttpMethod.GET, "/route", new TestResolve("actionName"), "name");

		assertThat(router.getNamedRoute("name").getRoute(), is("/route"));
		assertThat(router.getNamedRoute("otherName"), is(nullValue()));
		assertThat(router.getNamedRoute(null), is(nullValue()));
		assertThat(router.getNamedRoute(""), is(nullValue()));
	}

	@Test
	public void shouldAddGetRoutes() {
		router.get("/path/1", TestController.class, "controllerMethod1");
		router.get("/path/2", TestController.class, "controllerMethod2");
		router.get("/path/3", TestController.class, "controllerMethod3", "name3");
		router.get("/path/4", new Redirect("redirectTo"));
		router.get("/path/5", new Redirect("redirectTo"), "name5");
		router.add(HttpMethod.GET, "/path/6", new Redirect("redirectTo"), "name6");

		assertThat(router.has(HttpMethod.GET, "/path/1"), is(true));
		assertThat(router.has(HttpMethod.GET, "/path/2"), is(true));
		assertThat(router.has(HttpMethod.GET, "/path/3"), is(true));
		assertThat(router.has(HttpMethod.GET, "/path/4"), is(true));
		assertThat(router.has(HttpMethod.GET, "/path/5"), is(true));
		assertThat(router.has(HttpMethod.GET, "/path/6"), is(true));
		assertThat(router.has("name3"), is(true));
		assertThat(router.has("name5"), is(true));
		assertThat(router.has("name6"), is(true));
	}

	@Test
	public void shouldAddPostRoutes() {
		router.post("/path/1", TestController.class, "controllerMethod1");
		router.post("/path/2", TestController.class, "controllerMethod2");
		router.post("/path/3", TestController.class, "controllerMethod3", "name3");
		router.post("/path/4", new Redirect("redirectTo"));
		router.post("/path/5", new Redirect("redirectTo"), "name5");
		router.add(HttpMethod.POST, "/path/6", new Redirect("redirectTo"), "name6");

		assertThat(router.has(HttpMethod.POST, "/path/1"), is(true));
		assertThat(router.has(HttpMethod.POST, "/path/2"), is(true));
		assertThat(router.has(HttpMethod.POST, "/path/3"), is(true));
		assertThat(router.has(HttpMethod.POST, "/path/4"), is(true));
		assertThat(router.has(HttpMethod.POST, "/path/5"), is(true));
		assertThat(router.has(HttpMethod.POST, "/path/6"), is(true));
		assertThat(router.has("name3"), is(true));
		assertThat(router.has("name5"), is(true));
		assertThat(router.has("name6"), is(true));
	}

	@Test
	public void shouldAddPutRoutes() {
		router.put("/path/1", TestController.class, "controllerMethod1");
		router.put("/path/2", TestController.class, "controllerMethod2");
		router.put("/path/3", TestController.class, "controllerMethod3", "name3");
		router.put("/path/4", new Redirect("redirectTo"));
		router.put("/path/5", new Redirect("redirectTo"), "name5");
		router.add(HttpMethod.PUT, "/path/6", new Redirect("redirectTo"), "name6");

		assertThat(router.has(HttpMethod.PUT, "/path/1"), is(true));
		assertThat(router.has(HttpMethod.PUT, "/path/2"), is(true));
		assertThat(router.has(HttpMethod.PUT, "/path/3"), is(true));
		assertThat(router.has(HttpMethod.PUT, "/path/4"), is(true));
		assertThat(router.has(HttpMethod.PUT, "/path/5"), is(true));
		assertThat(router.has(HttpMethod.PUT, "/path/6"), is(true));
		assertThat(router.has("name3"), is(true));
		assertThat(router.has("name5"), is(true));
		assertThat(router.has("name6"), is(true));
	}

	@Test
	public void shouldAddDeleteRoutes() {
		router.delete("/path/1", TestController.class, "controllerMethod1");
		router.delete("/path/2", TestController.class, "controllerMethod2");
		router.delete("/path/3", TestController.class, "controllerMethod3", "name3");
		router.delete("/path/4", new Redirect("redirectTo"));
		router.delete("/path/5", new Redirect("redirectTo"), "name5");
		router.add(HttpMethod.DELETE, "/path/6", new Redirect("redirectTo"), "name6");

		assertThat(router.has(HttpMethod.DELETE, "/path/1"), is(true));
		assertThat(router.has(HttpMethod.DELETE, "/path/2"), is(true));
		assertThat(router.has(HttpMethod.DELETE, "/path/3"), is(true));
		assertThat(router.has(HttpMethod.DELETE, "/path/4"), is(true));
		assertThat(router.has(HttpMethod.DELETE, "/path/5"), is(true));
		assertThat(router.has(HttpMethod.DELETE, "/path/6"), is(true));
		assertThat(router.has("name3"), is(true));
		assertThat(router.has("name5"), is(true));
		assertThat(router.has("name6"), is(true));
	}

	@Test
	public void shouldAllowRemovalOfRoutesByMethodAndPath() {
		assertThat(router.has(HttpMethod.GET, "/route"), is(false));
		assertThat(router.has("name"), is(false));

		router.get("/route", new Redirect("/redirect"), "name");

		assertThat(router.has(HttpMethod.GET, "/route"), is(true));
		assertThat(router.has("name"), is(true));

		router.remove(HttpMethod.GET, "/route");

		assertThat(router.has(HttpMethod.GET, "/route"), is(false));
		assertThat(router.has("name"), is(false));
	}

	@Test
	public void shouldAllowRemovalOfRoutesByName() {
		assertThat(router.has(HttpMethod.GET, "/route"), is(false));
		assertThat(router.has("name"), is(false));

		router.get("/route", new Redirect("/redirect"), "name");

		assertThat(router.has(HttpMethod.GET, "/route"), is(true));
		assertThat(router.has("name"), is(true));

		router.remove("name");

		assertThat(router.has(HttpMethod.GET, "/route"), is(false));
		assertThat(router.has("name"), is(false));
	}

	@Test
	public void shouldDoNothingWhenRemoveRouteThatDoesntExist() {
		router.get("/route", new Redirect("/redirect"), "name");

		assertThat(router.has(HttpMethod.GET, "/route"), is(true));
		assertThat(router.has("name"), is(true));

		router.remove("non-existant");
		router.remove(HttpMethod.GET, "/nope");
		router.remove(null, null);
		router.remove(null);

		assertThat(router.has(HttpMethod.GET, "/route"), is(true));
		assertThat(router.has("name"), is(true));
	}

	@Test
	public void shouldListRoutes() {
		Router router = new Router();
		router.addResolver(TestResolve.class, new TestRouteResolver());
		router.add(HttpMethod.GET, "/path/*.jpg", new TestResolve("action1"), null);
		router.add(HttpMethod.PUT, "/path/*.jpg", new TestResolve("action2"), "PUT");

		// @formatter:off
		String expected = 
			"GET     /path/*.jpg                                                       : action1\n" +
			"PUT     /path/*.jpg                                                  (PUT): action2\n";
		// @formatter:on
		assertThat(router.listRoutes(), is(expected));
	}

	@SuppressWarnings("unused")
	private static class TestController {
		public String controllerMethod1() {
			return null;
		}

		public String controllerMethod2() {
			return null;
		}

		public String controllerMethod3() {
			return null;
		}
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
		public TestResolve resolve(TestResolve action, Request req, Response resp) throws RouteResolverException {
			return action;
		}
	}
}
