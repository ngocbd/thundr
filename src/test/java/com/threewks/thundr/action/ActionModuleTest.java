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
package com.threewks.thundr.action;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.action.method.ActionInterceptorRegistry;
import com.threewks.thundr.action.method.MethodAction;
import com.threewks.thundr.action.method.MethodActionResolver;
import com.threewks.thundr.action.method.bind.ActionMethodBinderRegistry;
import com.threewks.thundr.action.redirect.RedirectAction;
import com.threewks.thundr.action.redirect.RedirectActionResolver;
import com.threewks.thundr.action.rewrite.RewriteAction;
import com.threewks.thundr.action.rewrite.RewriteActionResolver;
import com.threewks.thundr.action.staticResource.StaticResourceAction;
import com.threewks.thundr.action.staticResource.StaticResourceActionResolver;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.route.Routes;
import com.threewks.thundr.test.mock.servlet.MockServletContext;

public class ActionModuleTest {

	private UpdatableInjectionContext injectionContext;
	private ServletContext servletContext = new MockServletContext();
	private Routes routes;

	@Before
	public void before() {
		injectionContext = new InjectionContextImpl();
		routes = new Routes();
		injectionContext.inject(routes).as(Routes.class);
		injectionContext.inject(servletContext).as(ServletContext.class);
	}

	@Test
	public void shouldProvideMethodActionResolverToInjectionContext() {
		new ActionModule().configure(injectionContext);
		assertThat(injectionContext.get(MethodActionResolver.class), is(notNullValue()));
		assertThat(injectionContext.get(ActionInterceptorRegistry.class), is(notNullValue()));
		assertThat(injectionContext.get(ActionMethodBinderRegistry.class), is(notNullValue()));
	}

	@Test
	public void shouldRegisterStandardThundrActionResolvers() {
		new ActionModule().configure(injectionContext);

		assertThat(routes.getActionResolver(MethodAction.class) instanceof MethodActionResolver, is(true));
		assertThat(routes.getActionResolver(StaticResourceAction.class) instanceof StaticResourceActionResolver, is(true));
		assertThat(routes.getActionResolver(RedirectAction.class) instanceof RedirectActionResolver, is(true));
		assertThat(routes.getActionResolver(RewriteAction.class) instanceof RewriteActionResolver, is(true));

	}
}
