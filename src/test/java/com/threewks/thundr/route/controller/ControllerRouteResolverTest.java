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

import static com.atomicleopard.expressive.Expressive.map;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.threewks.thundr.bind.BinderRegistry;
import com.threewks.thundr.bind.http.HttpBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.injection.InjectorBuilder;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.controller.Controller;
import com.threewks.thundr.route.controller.Interceptor;
import com.threewks.thundr.route.controller.ControllerRouteResolver;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.transformer.TransformerManager;

public class ControllerRouteResolverTest {
	private ControllerRouteResolver resolver;
	private UpdatableInjectionContext injectionContext;
	private HttpServletRequest req = mock(HttpServletRequest.class);
	private HttpServletResponse resp = mock(HttpServletResponse.class);
	private Map<String, String> pathVars = map();
	
	private FilterRegistry filters = new FilterRegistry();
	private BinderRegistry binderRegistry;
	private TransformerManager transformerManager;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void before() {
		transformerManager = TransformerManager.createWithDefaults();
		ParameterBinderRegistry parameterBinderRegistry = new ParameterBinderRegistry(transformerManager);
		ParameterBinderRegistry.addDefaultBinders(parameterBinderRegistry);

		binderRegistry = new BinderRegistry();
		BinderRegistry.registerDefaultBinders(binderRegistry, parameterBinderRegistry, transformerManager);
		
		injectionContext = mock(UpdatableInjectionContext.class);
		InjectorBuilder<Class> injectionBuilder = mock(InjectorBuilder.class);
		when(injectionContext.inject(Mockito.any(Class.class))).thenReturn(injectionBuilder);
		
		resolver = new ControllerRouteResolver(injectionContext, filters, binderRegistry);
		when(req.getContentType()).thenReturn(ContentType.ApplicationFormUrlEncoded.value());
	}

	@Test
	public void shouldAllowRegistrationOfActionInterceptors() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		resolver.registerInterceptor(TestAnnotation.class, registeredInterceptor);
		Interceptor<?> interceptor = resolver.interceptor(TestAnnotation.class);
		assertThat(interceptor, is(notNullValue()));
		assertThat(interceptor == registeredInterceptor, is(true));
	}

	@Test
	public void shouldReplaceExistingInterceptorWithNewInterceptor() {
		TestActionInterceptor registeredInterceptor1 = new TestActionInterceptor(null, null, null);
		TestActionInterceptor registeredInterceptor2 = new TestActionInterceptor(null, null, null);
		resolver.registerInterceptor(TestAnnotation.class, registeredInterceptor1);
		resolver.registerInterceptor(TestAnnotation.class, registeredInterceptor2);
		Interceptor<?> interceptor = resolver.interceptor(TestAnnotation.class);
		assertThat(interceptor, is(notNullValue()));
		assertThat(interceptor == registeredInterceptor1, is(false));
		assertThat(interceptor == registeredInterceptor2, is(true));
	}

	@Test
	public void shouldFindRegisteredInterceptorsForMethod() throws SecurityException, NoSuchMethodException {
		Method method = ControllerRouteResolverTest.class.getMethod("intercept");
		TestAnnotation annotation = method.getAnnotation(TestAnnotation.class);

		Map<Annotation, Interceptor<Annotation>> interceptors = resolver.findInterceptors(method);
		assertThat(interceptors.isEmpty(), is(true));

		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		resolver.registerInterceptor(TestAnnotation.class, registeredInterceptor);

		interceptors = resolver.findInterceptors(method);
		assertThat(interceptors.isEmpty(), is(false));
		assertThat(interceptors.get(annotation).equals(registeredInterceptor), is(true));
	}

	@Test
	public void shouldInvokeFilterBeforeActionMethod() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).before(HttpMethod.GET, req, resp);
		assertThat(registeredInterceptor.beforeInvoked, is(true));
	}

	@Test
	public void shouldReturnViewFromFilterAndSkipControllerInterceptorsAndOtherFilters() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		when(filter.before(HttpMethod.GET, req, resp)).thenReturn("Filter Result");

		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		Object view = resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).before(HttpMethod.GET, req, resp);
		assertThat(registeredInterceptor.beforeInvoked, is(false));
		assertThat(view, is((Object) "Filter Result"));
	}

	@Test
	public void shouldInvokeInterceptorBeforeActionMethod() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		assertThat(registeredInterceptor.beforeInvoked, is(false));
		resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);
		assertThat(registeredInterceptor.beforeInvoked, is(true));
	}

	@Test
	public void shouldInvokeInterceptorBeforeActionMethodNotInvokingMethodIfSomethingIsReturned() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor("Expected Before", null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		assertThat(registeredInterceptor.beforeInvoked, is(false));
		assertThat((String) resolver.resolve(action, HttpMethod.GET, req, resp, pathVars), is("Expected Before"));
		assertThat(registeredInterceptor.beforeInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(true));
		assertThat(registeredInterceptor.exceptionInvoked, is(false));
	}

	@Test
	public void shouldBindArgumentsAfterInvokingBeforeInterceptorAllowingBindingsToUseValuesModifiedByInterceptor() {
		@SuppressWarnings("unchecked")
		Interceptor<TestAnnotation> registeredInterceptor = new Interceptor<TestAnnotation>() {
			@Override
			public String before(TestAnnotation annotation, HttpServletRequest req, HttpServletResponse resp) {
				req.setAttribute("name", "value");
				return null;
			}

			@Override
			public String after(TestAnnotation annotation, Object result, HttpServletRequest req, HttpServletResponse resp) {
				return null;
			}

			@Override
			public String exception(TestAnnotation annotation, Exception e, HttpServletRequest req, HttpServletResponse resp) {
				return null;
			}

		};
		Controller action = prepareActionMethod("interceptWithValue", registeredInterceptor);

		req = new MockHttpServletRequest();
		Object resolvedValue = resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);
		assertThat(resolvedValue, is((Object) "value"));
	}

	@Test
	public void shouldInvokeFilterAfterActionMethodAndInterceptors() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).after(HttpMethod.GET, null, req, resp);
		assertThat(registeredInterceptor.afterInvoked, is(true));
	}

	@Test
	public void shouldReturnViewFromAfterFilterAfterRunningInterceptors() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		when(filter.after(HttpMethod.GET, null, req, resp)).thenReturn("Filter Result");

		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		Object view = resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).after(HttpMethod.GET, null, req, resp);
		assertThat(registeredInterceptor.afterInvoked, is(true));
		assertThat(registeredInterceptor.exceptionInvoked, is(false));
		assertThat(view, is((Object) "Filter Result"));
	}

	@Test
	public void shouldInvokeInterceptorAfterActionMethod() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		assertThat(registeredInterceptor.afterInvoked, is(false));
		resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);
		assertThat(registeredInterceptor.afterInvoked, is(true));
	}

	@Test
	public void shouldInvokeInterceptorAfterActionMethodReturingInterceptorValueIfNotNull() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, "Expected After", null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		assertThat((String) resolver.resolve(action, HttpMethod.GET, req, resp, pathVars), is("Expected After"));
		assertThat(registeredInterceptor.beforeInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(true));
		assertThat(registeredInterceptor.exceptionInvoked, is(false));
	}

	@Test
	public void shouldInvokeAfterFiltersPassingTheResultOfAfterInterceptors() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);

		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, "Expected After", null);
		Controller action = prepareActionMethod("intercept", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		assertThat((String) resolver.resolve(action, HttpMethod.GET, req, resp, pathVars), is("Expected After"));
		assertThat(registeredInterceptor.beforeInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(true));
		assertThat(registeredInterceptor.exceptionInvoked, is(false));
		verify(filter).after(HttpMethod.GET, "Expected After", req, resp);
	}

	@Test
	public void shouldInvokeFilterOnException() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, "invoked");
		Controller action = prepareActionMethod("interceptException", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).exception(eq(HttpMethod.GET), Mockito.any(Exception.class), eq(req), eq(resp));
		assertThat(registeredInterceptor.exceptionInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(false));
	}

	@Test
	public void shouldReturnViewFromExceptionFilterAfterRunningInterceptors() {
		Filter filter = mock(Filter.class);
		filters.add("/*", filter);
		when(filter.exception(eq(HttpMethod.GET), Mockito.any(Exception.class), eq(req), eq(resp))).thenReturn("Filter Result");

		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, "invoked");
		Controller action = prepareActionMethod("interceptException", registeredInterceptor);

		when(req.getRequestURI()).thenReturn("/request");

		Object view = resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);

		verify(filter, times(1)).exception(eq(HttpMethod.GET), Mockito.any(Exception.class), eq(req), eq(resp));
		assertThat(registeredInterceptor.exceptionInvoked, is(true));
		assertThat(view, is((Object) "Filter Result"));
	}

	@Test
	public void shouldInvokeInterceptorOnExceptionFromActionMethod() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, "invoked");
		Controller action = prepareActionMethod("interceptException", registeredInterceptor);

		assertThat(registeredInterceptor.exceptionInvoked, is(false));
		Object view = resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);
		assertThat(view, is((Object) "invoked"));
		assertThat(registeredInterceptor.exceptionInvoked, is(true));
	}

	@Test
	public void shouldInvokeInterceptorWhenExceptionInActionMethodReturingInterceptorValueIfNotNull() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, "Expected Exception");
		Controller action = prepareActionMethod("interceptException", registeredInterceptor);

		assertThat((String) resolver.resolve(action, HttpMethod.GET, req, resp, pathVars), is("Expected Exception"));
		assertThat(registeredInterceptor.beforeInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(false));
		assertThat(registeredInterceptor.exceptionInvoked, is(true));
	}

	@Test
	public void shouldInvokeInterceptorWhenExceptionInActionMethodThrowingIfInterceptorValueIsNull() {
		TestActionInterceptor registeredInterceptor = new TestActionInterceptor(null, null, null);
		Controller action = prepareActionMethod("interceptException", registeredInterceptor);

		try {
			resolver.resolve(action, HttpMethod.GET, req, resp, pathVars);
			fail("Expected an exception");
		} catch (RuntimeException e) {
			// expected
		}
		assertThat(registeredInterceptor.beforeInvoked, is(true));
		assertThat(registeredInterceptor.afterInvoked, is(false));
		assertThat(registeredInterceptor.exceptionInvoked, is(true));
	}

	@Test
	public void shouldReturnNullIfCannotCreateAction() {
		assertThat(resolver.createActionIfPossible(null), is(nullValue()));
		assertThat(resolver.createActionIfPossible(""), is(nullValue()));
		assertThat(resolver.createActionIfPossible("Junk"), is(nullValue()));
		assertThat(resolver.createActionIfPossible(".method"), is(nullValue()));
		assertThat(resolver.createActionIfPossible(ControllerRouteResolverTest.class.getName()), is(nullValue()));
		assertThat(resolver.createActionIfPossible(ControllerRouteResolverTest.class.getName() + ".aintNoSuchMethod"), is(nullValue()));
	}

	@Test
	public void shouldCreateActionMethodClassAtInitialise() {
		resolver = spy(resolver);
		Controller methodAction = new Controller(ControllerRouteResolverTest.class, "intercept");
		resolver.initialise(methodAction);
		verify(resolver).createController(Mockito.any(Controller.class));
	}

	private Controller prepareActionMethod(String method, Interceptor<TestAnnotation> registeredInterceptor) {
		when(injectionContext.get(ControllerRouteResolverTest.class)).thenReturn(this);
		resolver.registerInterceptor(TestAnnotation.class, registeredInterceptor);
		Controller action = resolver.createActionIfPossible(ControllerRouteResolverTest.class.getName() + "." + method);
		assertThat(action, is(notNullValue()));
		return action;
	}

	@TestAnnotation("Parameter")
	public void intercept() {

	}

	@TestAnnotation("Parameter")
	public String interceptWithValue(String name) {
		return name;
	}

	@TestAnnotation("Parameter")
	public void interceptException() {
		throw new RuntimeException("Expected");
	}

	@SuppressWarnings("unchecked")
	private class TestActionInterceptor implements Interceptor<TestAnnotation> {
		private String onBefore = null;
		private String onAfter = null;
		private String onException = null;
		public boolean exceptionInvoked;
		public boolean afterInvoked;
		public boolean beforeInvoked;

		public TestActionInterceptor(String onBefore, String onAfter, String onException) {
			super();
			this.onBefore = onBefore;
			this.onAfter = onAfter;
			this.onException = onException;
		}

		@Override
		public String before(TestAnnotation annotation, HttpServletRequest req, HttpServletResponse resp) {
			beforeInvoked = true;
			return onBefore;
		}

		@Override
		public String after(TestAnnotation annotation, Object result, HttpServletRequest req, HttpServletResponse resp) {
			afterInvoked = true;
			return onAfter;
		}

		@Override
		public String exception(TestAnnotation annotation, Exception e, HttpServletRequest req, HttpServletResponse resp) {
			exceptionInvoked = true;
			return onException;
		}
	}
}
