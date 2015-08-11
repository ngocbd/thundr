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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atomicleopard.expressive.Cast;
import com.threewks.thundr.http.RequestThreadLocal;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.ModulesModule;
import com.threewks.thundr.request.MutableRequestContainer;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.servlet.ServletRequest;
import com.threewks.thundr.request.servlet.ServletResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import com.threewks.thundr.test.mock.servlet.MockServletConfig;
import com.threewks.thundr.test.mock.servlet.MockServletContext;
import com.threewks.thundr.view.ViewRenderer;
import com.threewks.thundr.view.ViewResolver;
import com.threewks.thundr.view.ViewResolverNotFoundException;
import com.threewks.thundr.view.ViewResolverRegistry;

public class ThundrServletTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private ThundrServlet servlet;
	private Thundr thundr;

	private UpdatableInjectionContext injectionContext;
	private MockHttpServletResponse resp = new MockHttpServletResponse();
	private Router router = mock(Router.class);
	private ViewResolverRegistry viewResolverRegistry;

	@Before
	public void before() throws ServletException {
		ServletContext servletContext = new MockServletContext();
		ServletConfig config = new MockServletConfig(servletContext);
		servlet = new ThundrServlet() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Thundr createAndStartThundr() {
				Thundr thundr = new Thundr();
				thundr.injectionContext.inject(ThundrServletTest.class.getPackage().getName() + ".TestApplicationModule").named(ModulesModule.ApplicationClassProperty).as(String.class);
				thundr.start();
				return thundr;
			}
		};
		servlet.init(config);
		servlet.thundr = spy(servlet.thundr);
		thundr = servlet.thundr;
		injectionContext = thundr.injectionContext;

		when(router.invoke(Mockito.any(Request.class), Mockito.any(Response.class))).thenReturn("View Name");
		injectionContext.inject(router).as(Router.class);

		viewResolverRegistry = new ViewResolverRegistry();
		viewResolverRegistry.addResolver(String.class, new ViewResolver<String>() {
			@Override
			public void resolve(Request req, Response resp, String viewResult) {
			}
		});
		injectionContext.inject(viewResolverRegistry).as(ViewResolverRegistry.class);
	}

	@Ignore("TODO - NAO - This should be relocated or reinstituted")
	@Test
	public void shouldInitializeThundrServletInit() throws ServletException {
		// reset the servlet state which was already initialised in the before() block
		servlet.thundr = null;

		ServletContext servletContext = new MockServletContext();
		ServletConfig config = new MockServletConfig(servletContext);

		servlet.init(config);
		UpdatableInjectionContext injectionContext = servlet.thundr.injectionContext;
		assertThat(injectionContext, is(notNullValue()));
		assertThat(injectionContext.get(ServletContext.class), is(servletContext));
		assertThat(servletContext.getAttribute("injectionContext"), is((Object) injectionContext));
	}

	@Test
	public void shouldWrapExceptionInServletException() throws ServletException {
		thrown.expect(ServletException.class);

		ServletContext servletContext = new MockServletContext();
		ServletConfig config = new MockServletConfig(servletContext);
		servlet = spy(servlet);
		doThrow(new RuntimeException("Expected")).when(servlet).createAndStartThundr();

		servlet.init(config);
	}

	@Test
	public void shouldApplyGetRouteWhenDoGet() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("GET");
		servlet = spy(servlet);
		servlet.doGet(req, resp);
		verify(servlet).applyRoute(HttpMethod.GET, req, resp);
	}

	@Test
	public void shouldApplyGetRouteWhenDoGetIgnoringCase() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("gEt");
		servlet = spy(servlet);
		servlet.doGet(req, resp);
		verify(servlet).applyRoute(HttpMethod.GET, req, resp);
	}

	@Test
	public void shouldApplyPostRouteWhenDoPost() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.POST, req, resp);
	}

	@Test
	public void shouldApplyPutRouteWhenDoPostWithPutMethodParameter() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.parameter("_method", "pUT");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.PUT, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoPostWithDeleteMethodParameterIgnoringCase() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.parameter("_MEthOD", "deleTE");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.DELETE, req, resp);
	}

	@Test
	public void shouldApplyPutRouteWhenDoPostWithXHttpMethodOverrideHeader() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.header("X-HTTP-Method-Override", "pUT");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.PUT, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoPostWithXHttpMethodOverrideHeaderIgnoringCase() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.header("X-HttP-METHOD-Override", "DELETE");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.DELETE, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoPostWithDeleteMethodParameter() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.parameter("_method", "dEleTE");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.DELETE, req, resp);
	}

	@Test
	public void shouldApplyPatchRouteWhenDoPostWithPatchMethodParameter() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("POST");
		req.parameter("_method", "pATCh");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.PATCH, req, resp);
	}

	@Test
	public void shouldApplyPutRouteWhenDoPut() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("PUT");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.PUT, req, resp);
	}

	@Test
	public void shouldApplyPatchRouteWhenDoPatch() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("PATCH");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.PATCH, req, resp);
	}

	@Test
	public void shouldApplyDeleteRouteWhenDoDelete() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("DELETE");
		servlet = spy(servlet);
		servlet.service(req, resp);
		verify(servlet).applyRoute(HttpMethod.DELETE, req, resp);
	}

	@Test
	public void shouldApplyGetRouteWhenHeadRequestMade() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("HEAD");
		servlet = spy(servlet);
		servlet.service(req, resp);
		// we expect the response to be wrapped in the NoBodyResponse in the base HttpServlet class
		verify(servlet).doGet(Mockito.eq(req), Mockito.any(HttpServletResponse.class));
		verify(servlet).applyRoute(Mockito.eq(HttpMethod.GET), Mockito.eq(req), Mockito.any(HttpServletResponse.class));
	}

	@Test
	public void shouldReturnNotImplementedWhenRequestingUnknownMethod() throws ServletException, IOException {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("NOT A REAL METHOD");
		servlet = spy(servlet);
		servlet.service(req, resp);
		// we expect the response to be wrapped in the NoBodyResponse in the base HttpServlet class
		verify(servlet, times(0)).applyRoute(Mockito.any(HttpMethod.class), Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class));
		assertThat(resp.status(), is(HttpServletResponse.SC_NOT_IMPLEMENTED));
	}

	@Test
	public void shouldSetAndClearRequestAndResponseIntoRequestScope() throws ServletException, IOException {
		final MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("GET");
		MutableRequestContainer requestContainer = injectionContext.get(MutableRequestContainer.class);

		doAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Request request = requestContainer.getRequest();
				Response response = requestContainer.getResponse();
				assertThat(Cast.is(request, ServletRequest.class), is(true));
				assertThat(Cast.is(response, ServletResponse.class), is(true));
				assertThat(request.getRawRequest(HttpServletRequest.class), is(req));
				assertThat(response.getRawResponse(HttpServletResponse.class), is(resp));
				return "view";
			}
		}).when(thundr).applyRoute(Mockito.any(Request.class), Mockito.any(Response.class), Mockito.any(ViewRenderer.class));

		servlet.service(req, resp);
		assertThat(requestContainer.getRequest(), is(nullValue()));
		assertThat(requestContainer.getResponse(), is(nullValue()));
	}

	@Test
	public void shouldSetAndClearRequestAndResponseIntoRequestScopeOnException() throws ServletException, IOException {
		thrown.expect(ViewResolverNotFoundException.class);
		final MockHttpServletRequest req = new MockHttpServletRequest();
		req.method("GET");

		MutableRequestContainer requestContainer = injectionContext.get(MutableRequestContainer.class);

		doAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Request request = requestContainer.getRequest();
				Response response = requestContainer.getResponse();
				assertThat(Cast.is(request, ServletRequest.class), is(true));
				assertThat(Cast.is(response, ServletResponse.class), is(true));
				assertThat(request.getRawRequest(HttpServletRequest.class), is(req));
				assertThat(response.getRawResponse(HttpServletResponse.class), is(resp));
				throw new ViewResolverNotFoundException("Intentional");
			}
		}).when(thundr).applyRoute(Mockito.any(Request.class), Mockito.any(Response.class), Mockito.any(ViewRenderer.class));

		try {
			servlet.service(req, resp);
		} catch (RuntimeException e) {
			assertThat(RequestThreadLocal.getRequest(), is(nullValue()));
			assertThat(RequestThreadLocal.getResponse(), is(nullValue()));
			throw e;
		}
	}

	@Test
	public void shouldStopModulesOnDestroy() {
		servlet.destroy();

		verify(thundr).stop();
	}
}
