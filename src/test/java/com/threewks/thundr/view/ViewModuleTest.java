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
package com.threewks.thundr.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.route.RouteNotFoundException;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.view.exception.ExceptionViewResolver;
import com.threewks.thundr.view.exception.HttpStatusExceptionViewResolver;
import com.threewks.thundr.view.exception.RouteNotFoundViewResolver;
import com.threewks.thundr.view.file.FileView;
import com.threewks.thundr.view.file.FileViewResolver;
import com.threewks.thundr.view.json.JsonNegotiator;
import com.threewks.thundr.view.json.JsonView;
import com.threewks.thundr.view.json.JsonViewResolver;
import com.threewks.thundr.view.jsonp.JsonpNegotiator;
import com.threewks.thundr.view.jsonp.JsonpView;
import com.threewks.thundr.view.jsonp.JsonpViewResolver;
import com.threewks.thundr.view.negotiating.NegotiatingView;
import com.threewks.thundr.view.negotiating.NegotiatingViewResolver;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistry;
import com.threewks.thundr.view.negotiating.ViewNegotiatorRegistryImpl;
import com.threewks.thundr.view.redirect.RedirectView;
import com.threewks.thundr.view.redirect.RedirectViewResolver;
import com.threewks.thundr.view.redirect.RouteRedirectView;
import com.threewks.thundr.view.redirect.RouteRedirectViewResolver;
import com.threewks.thundr.view.string.StringView;
import com.threewks.thundr.view.string.StringViewResolver;

public class ViewModuleTest {
	private ViewModule module = new ViewModule();
	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Test
	public void shouldInjectViewDependenciesOnInitialise() {
		module.initialise(injectionContext);

		assertThat(injectionContext.contains(ViewResolverRegistry.class), is(true));
		assertThat(injectionContext.contains(ViewNegotiatorRegistry.class), is(true));
		assertThat(injectionContext.contains(GlobalModel.class), is(true));
	}

	@Test
	public void shouldAddStandardViewResolversWhenConfigure() {
		ViewResolverRegistry registry = new ViewResolverRegistry();
		injectionContext.inject(registry).as(ViewResolverRegistry.class);
		module.initialise(injectionContext);
		module.configure(injectionContext);

		assertThat(registry.findViewResolver(new Throwable()) instanceof ExceptionViewResolver, is(true));
		assertThat(registry.findViewResolver(new HttpStatusException(StatusCode.BadGateway, "")) instanceof HttpStatusExceptionViewResolver, is(true));
		assertThat(registry.findViewResolver(new RouteNotFoundException("")) instanceof RouteNotFoundViewResolver, is(true));
		assertThat(registry.findViewResolver(new RouteRedirectView("")) instanceof RouteRedirectViewResolver, is(true));
		assertThat(registry.findViewResolver(new RedirectView("")) instanceof RedirectViewResolver, is(true));
		assertThat(registry.findViewResolver(new JsonView("")) instanceof JsonViewResolver, is(true));
		assertThat(registry.findViewResolver(new JsonpView("")) instanceof JsonpViewResolver, is(true));
		assertThat(registry.findViewResolver(new FileView("", new byte[0], "")) instanceof FileViewResolver, is(true));
		assertThat(registry.findViewResolver(new StringView("")) instanceof StringViewResolver, is(true));
		assertThat(registry.findViewResolver(new NegotiatingView("")) instanceof NegotiatingViewResolver, is(true));

		assertThat(injectionContext.contains(HttpStatusExceptionViewResolver.class), is(true));
		assertThat(injectionContext.contains(ExceptionViewResolver.class), is(true));
		assertThat(injectionContext.contains(NegotiatingViewResolver.class), is(true));
	}

	@Test
	public void shouldAddRoutesToGlobalModelOnConfigure() {
		GlobalModel globalModel = new GlobalModel();
		ViewResolverRegistry registry = new ViewResolverRegistry();
		Router router = new Router();
		injectionContext.inject(router).as(Router.class);
		injectionContext.inject(globalModel).as(GlobalModel.class);
		injectionContext.inject(registry).as(ViewResolverRegistry.class);
		module.initialise(injectionContext);

		module.configure(injectionContext);

		assertThat(globalModel.get("router"), is((Object) router));
	}

	@Test
	public void shouldAddDefaultNegotiatorsOnConfigure() {
		ViewNegotiatorRegistry registry = new ViewNegotiatorRegistryImpl();
		injectionContext.inject(registry).as(ViewNegotiatorRegistry.class);
		module.initialise(injectionContext);

		module.configure(injectionContext);

		assertThat(registry.getDefaultNegotiator(), instanceOf(JsonNegotiator.class));
		assertThat(registry.getNegotiator("application/json"), instanceOf(JsonNegotiator.class));
		assertThat(registry.getNegotiator("application/javascript"), instanceOf(JsonpNegotiator.class));
	}
}
