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

import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.injection.BaseModule;
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

import jodd.util.MimeTypes;

public class ViewModule extends BaseModule {
	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		injectionContext.inject(ViewResolverRegistry.class).as(ViewResolverRegistry.class);
		injectionContext.inject(ViewNegotiatorRegistryImpl.class).as(ViewNegotiatorRegistry.class);
		injectionContext.inject(GlobalModel.class).as(GlobalModel.class);
	}

	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		GlobalModel globalModel = injectionContext.get(GlobalModel.class);
		globalModel.put("router", injectionContext.get(Router.class));

		ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);

		addViewResolvers(viewResolverRegistry, injectionContext, globalModel);
	}

	protected void addViewResolvers(ViewResolverRegistry viewResolverRegistry, UpdatableInjectionContext injectionContext, GlobalModel globalModel) {
		Router router = injectionContext.get(Router.class);
		ViewNegotiatorRegistry viewNegotiatorRegistry = injectionContext.get(ViewNegotiatorRegistry.class);

		ExceptionViewResolver exceptionViewResolver = new ExceptionViewResolver();
		HttpStatusExceptionViewResolver statusViewResolver = new HttpStatusExceptionViewResolver();
		NegotiatingViewResolver negotiatingViewResolver = new NegotiatingViewResolver(viewResolverRegistry, viewNegotiatorRegistry);

		injectionContext.inject(exceptionViewResolver).as(ExceptionViewResolver.class);
		injectionContext.inject(statusViewResolver).as(HttpStatusExceptionViewResolver.class);
		injectionContext.inject(negotiatingViewResolver).as(NegotiatingViewResolver.class);

		// Register built in content negotiators

		viewNegotiatorRegistry.setDefaultNegotiator(new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator(MimeTypes.MIME_APPLICATION_JSON, new JsonNegotiator());
		viewNegotiatorRegistry.addNegotiator(MimeTypes.MIME_APPLICATION_JAVASCRIPT, new JsonpNegotiator());

		viewResolverRegistry.addResolver(Throwable.class, exceptionViewResolver);
		viewResolverRegistry.addResolver(HttpStatusException.class, statusViewResolver);
		viewResolverRegistry.addResolver(RouteNotFoundException.class, new RouteNotFoundViewResolver());
		viewResolverRegistry.addResolver(RouteRedirectView.class, new RouteRedirectViewResolver(router));
		viewResolverRegistry.addResolver(RedirectView.class, new RedirectViewResolver());
		viewResolverRegistry.addResolver(JsonView.class, new JsonViewResolver());
		viewResolverRegistry.addResolver(JsonpView.class, new JsonpViewResolver());
		viewResolverRegistry.addResolver(FileView.class, new FileViewResolver());
		viewResolverRegistry.addResolver(StringView.class, new StringViewResolver());
		viewResolverRegistry.addResolver(NegotiatingView.class, negotiatingViewResolver);
	}
}
