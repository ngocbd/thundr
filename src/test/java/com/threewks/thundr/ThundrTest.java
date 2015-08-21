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
package com.threewks.thundr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.threewks.thundr.configuration.ConfigurationModule;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.Module;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.Modules;
import com.threewks.thundr.module.ModulesModule;
import com.threewks.thundr.request.MutableRequestContainer;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.ThreadLocalRequestContainer;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.RouteResolverException;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.route.RouterModule;
import com.threewks.thundr.transformer.TransformerModule;
import com.threewks.thundr.view.BasicViewRenderer;
import com.threewks.thundr.view.ViewRenderer;
import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverNotFoundException;
import com.threewks.thundr.view.ViewResolverRegistry;

public class ThundrTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Thundr thundr = new Thundr();
	private UpdatableInjectionContext injectionContext;
	private Response resp;
	private Request req = new MockRequest(HttpMethod.GET, "/get/");
	private Router router = mock(Router.class);
	private ViewResolverRegistry viewResolverRegistry;
	private ViewRenderer viewRenderer;
	private MutableRequestContainer requestContainer;

	@Before
	public void before() {
		resp = mock(Response.class);
		when(resp.isUncommitted()).thenReturn(true);

		injectionContext = new InjectionContextImpl();
		thundr.injectionContext = injectionContext;

		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn("View Name");
		injectionContext.inject(router).as(Router.class);

		viewResolverRegistry = new ViewResolverRegistry();
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request req, Response resp, String viewResult) {
			}
		});
		injectionContext.inject(viewResolverRegistry).as(ViewResolverRegistry.class);
		requestContainer = new ThreadLocalRequestContainer();
		viewRenderer = new BasicViewRenderer(viewResolverRegistry);
		requestContainer.set(req, resp);
	}

	@After
	public void after() {
		requestContainer.clear();
	}

	@Test
	public void shouldInitializeInjectionContextAndModules() {
		thundr = new Thundr();
		assertThat(thundr.injectionContext, is(notNullValue()));
		assertThat(thundr.modules, is(notNullValue()));
	}

	@Test
	public void shouldInitModulesOnStart() {
		thundr = spy(new Thundr());
		when(thundr.getBaseModules()).thenReturn(Collections.<Class<? extends Module>> emptyList());
		thundr.start();

		verify(thundr).initModules(thundr.injectionContext, thundr.modules);
	}

	@Test
	public void shouldDependOnBaseModulesAndStartThem() {
		Modules modules = mock(Modules.class);
		thundr.initModules(injectionContext, modules);
		verify(modules).addModule(ConfigurationModule.class);
		verify(modules).addModule(ModulesModule.class);
		verify(modules).addModule(TransformerModule.class);
		verify(modules).addModule(RouterModule.class);
		verify(modules).runStartupLifecycle(injectionContext);
	}

	@Test
	public void shouldFindRouteDelegatingToRouter() {
		Route route = mock(Route.class);
		when(router.findMatchingRoute(HttpMethod.PATCH, "/a/b/c")).thenReturn(route);
		assertThat(thundr.findRoute(HttpMethod.PATCH, "/a/b/c"), is(route));
	}

	@Test
	public void shouldFindViewForResolvedRoute() {
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request req, Response resp, String viewResult) {
				resp.withStatusCode(StatusCode.ImATeapot);
			}
		});

		thundr.resolve(req, resp, viewRenderer);
		verify(resp).withStatusCode(StatusCode.ImATeapot);
	}

	@Test
	public void shouldNotResolveViewWhenNullViewResultReturned() {
		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn(null);

		viewResolverRegistry.addResolver(Object.class, new ViewResolver<Object>() {
			@Override
			public void resolve(Request req, Response resp, Object viewResult) {
				resp.withStatusCode(StatusCode.ImATeapot);
			}
		});

		thundr.resolve(req, resp, viewRenderer);
		verify(resp, never()).withStatusCode(Mockito.any(StatusCode.class));
	}

	@Test
	public void shouldCatchExceptionsFromViewResolversAndResolveExceptionWithExceptionView() {
		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn("View Name");

		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request req, Response resp, String viewResult) {
				throw new RuntimeException("Intentional Exception");
			}
		});
		viewResolverRegistry.addResolver(Exception.class, new ViewResolver<Exception>() {
			@Override
			public void resolve(Request req, Response resp, Exception viewResult) {
				resp.withStatusCode(StatusCode.ImATeapot);
			}
		});

		thundr.resolve(req, resp, viewRenderer);
		verify(resp).withStatusCode(StatusCode.ImATeapot);
	}

	@Test
	public void shouldCatchActionExceptionsFromViewResolversAndUnwrapThemBeforeResolvoingWithExceptionView() {
		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn("View Name");

		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request req, Response resp, String viewResult) {
				throw new RouteResolverException(new RuntimeException("Intentional Exception"), "");
			}
		});
		viewResolverRegistry.addResolver(RuntimeException.class, new ViewResolver<RuntimeException>() {
			@Override
			public void resolve(Request req, Response resp, RuntimeException viewResult) {
				resp.withStatusCode(StatusCode.ImATeapot);
			}
		});

		thundr.resolve(req, resp, viewRenderer);
		verify(resp).withStatusCode(StatusCode.ImATeapot);
	}

	@Test
	public void shouldCatchExceptionsFromViewResolversButDoNothingWhenResponseAlreadyCommitted() {
		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenThrow(new RuntimeException("Expected exception"));

		viewResolverRegistry.addResolver(Exception.class, new ViewResolver<Exception>() {
			@Override
			public void resolve(Request req, Response resp, Exception viewResult) {
				resp.withStatusCode(StatusCode.ImATeapot);
			}
		});

		when(resp.isUncommitted()).thenReturn(false);
		thundr.resolve(req, resp, viewRenderer);
		verify(resp, never()).withStatusCode(Mockito.any(StatusCode.class));
	}

	@Test
	public void shouldThrowViewResolverNotFoundIfNoMatchingViewResolverExists() {
		thrown.expect(ViewResolverNotFoundException.class);

		when(router.resolve(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn(false);

		thundr.resolve(req, resp, viewRenderer);
	}

	@Test
	public void shouldStopModulesOnStop() {
		thundr.modules = mock(Modules.class);
		thundr.stop();

		verify(thundr.modules).runStopLifecycle(injectionContext);
	}
}
