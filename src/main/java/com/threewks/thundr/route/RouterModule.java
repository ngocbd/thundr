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

import javax.servlet.ServletContext;

import com.threewks.thundr.bind.BinderModule;
import com.threewks.thundr.bind.BinderRegistry;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.route.controller.Controller;
import com.threewks.thundr.route.controller.ControllerRouteResolver;
import com.threewks.thundr.route.controller.FilterRegistry;
import com.threewks.thundr.route.controller.InterceptorRegistry;
import com.threewks.thundr.route.redirect.Redirect;
import com.threewks.thundr.route.redirect.RedirectRouteResolver;
import com.threewks.thundr.route.rewrite.Rewrite;
import com.threewks.thundr.route.rewrite.RewriteRouteResolver;
import com.threewks.thundr.route.staticResource.StaticResource;
import com.threewks.thundr.route.staticResource.StaticResourceRouteResolver;
import com.threewks.thundr.view.ViewModule;

public class RouterModule extends BaseModule {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		dependencyRegistry.addDependency(ViewModule.class);
		dependencyRegistry.addDependency(BinderModule.class);
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		injectionContext.inject(new Router()).as(Router.class);
		injectionContext.inject(new FilterRegistry()).as(FilterRegistry.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		Router router = injectionContext.get(Router.class);
		FilterRegistry filters = injectionContext.get(FilterRegistry.class);
		ServletContext servletContext = injectionContext.get(ServletContext.class);
		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);

		ControllerRouteResolver methodActionResolver = new ControllerRouteResolver(injectionContext, filters, binderRegistry);
		injectionContext.inject(methodActionResolver).as(ControllerRouteResolver.class);
		// The MethodActionResolver is special because we use it to perform controller interception
		injectionContext.inject(methodActionResolver).as(InterceptorRegistry.class);
		injectionContext.inject(methodActionResolver.getMethodBinderRegistry()).as(BinderRegistry.class);

		router.addResolver(Redirect.class, new RedirectRouteResolver());
		router.addResolver(Rewrite.class, new RewriteRouteResolver(router));
		router.addResolver(StaticResource.class, new StaticResourceRouteResolver(servletContext));
		router.addResolver(Controller.class, methodActionResolver);
	}
}
