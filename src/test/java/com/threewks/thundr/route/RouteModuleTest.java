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

import com.threewks.thundr.action.ActionException;
import com.threewks.thundr.action.ActionModule;
import com.threewks.thundr.action.TestAction;
import com.threewks.thundr.action.TestActionResolver;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.test.TestSupport;
import com.threewks.thundr.view.ViewModule;

public class RouteModuleTest {

	@Rule public ExpectedException thrown = ExpectedException.none();
	private RouteModule routeModule = new RouteModule();
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Test
	public void shouldDependOnViewResolverAndActionInjection() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		routeModule.requires(dependencyRegistry);
		assertThat(dependencyRegistry.hasDependency(ActionModule.class), is(true));
		assertThat(dependencyRegistry.hasDependency(ViewModule.class), is(true));
	}

	@Test
	public void shouldAddRoutesWhenInitialised() {
		routeModule.initialise(injectionContext);
		assertThat(injectionContext.contains(Routes.class), is(true));
	}

	@Test
	public void shouldAddRoutesFromRoutesFileOnStart() {
		TestSupport.setField(routeModule, "filename", "test-routes.json");
		Routes routes = new Routes();
		routes.addActionResolver(TestAction.class, new TestActionResolver());
		injectionContext.inject(routes).as(Routes.class);
		routeModule.start(injectionContext);

		assertThat(routes.findMatchingRoute("/route/1", RouteType.GET), is(notNullValue()));
	}

	@Test
	public void shouldNotThrowExceptionWhenUnableToLoadRoutesFile() {
		TestSupport.setField(routeModule, "filename", "non-existant.json");

		injectionContext.inject(new Routes()).as(Routes.class);
		routeModule.start(injectionContext);
	}

	@Test
	public void shouldThrowActionExceptionWhenLoadRoutesFileWithInvalidRoutes() {
		thrown.expect(ActionException.class);

		TestSupport.setField(routeModule, "filename", "invalid-routes.json");

		Routes routes = new Routes();
		routes.addActionResolver(TestAction.class, new TestActionResolver());
		injectionContext.inject(routes).as(Routes.class);
		routeModule.start(injectionContext);
	}
}
