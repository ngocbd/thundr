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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.route.controller.Controller;
import com.threewks.thundr.route.controller.ControllerInterceptorRegistry;
import com.threewks.thundr.route.controller.ControllerRouteResolver;
import com.threewks.thundr.route.redirect.Redirect;
import com.threewks.thundr.route.redirect.RedirectRouteResolver;
import com.threewks.thundr.route.rewrite.Rewrite;
import com.threewks.thundr.route.rewrite.RewriteRouteResolver;
import com.threewks.thundr.route.staticResource.StaticResource;
import com.threewks.thundr.route.staticResource.StaticResourceRouteResolver;
import com.threewks.thundr.view.ViewModule;

public class RouterModuleTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private RouterModule routeModule = new RouterModule();
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Test
	public void shouldDependOnViewResolverAndActionInjection() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		routeModule.requires(dependencyRegistry);
		assertThat(dependencyRegistry.hasDependency(ViewModule.class), is(true));
	}

	@Test
	public void shouldAddRoutesWhenInitialised() {
		routeModule.initialise(injectionContext);
		assertThat(injectionContext.contains(Router.class), is(true));
	}

	@Test
	public void shouldProvideMethodActionResolverToInjectionContext() {
		routeModule.initialise(injectionContext);
		routeModule.configure(injectionContext);
		assertThat(injectionContext.get(ControllerRouteResolver.class), is(notNullValue()));
		assertThat(injectionContext.get(ControllerInterceptorRegistry.class), is(notNullValue()));
	}

	@Test
	public void shouldRegisterStandardRouteResolvers() {
		routeModule.initialise(injectionContext);
		routeModule.configure(injectionContext);

		Router router = injectionContext.get(Router.class);

		assertThat(router.getResolver(Controller.class) instanceof ControllerRouteResolver, is(true));
		assertThat(router.getResolver(StaticResource.class) instanceof StaticResourceRouteResolver, is(true));
		assertThat(router.getResolver(Redirect.class) instanceof RedirectRouteResolver, is(true));
		assertThat(router.getResolver(Rewrite.class) instanceof RewriteRouteResolver, is(true));

	}
}
