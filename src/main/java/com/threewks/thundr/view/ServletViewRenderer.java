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
package com.threewks.thundr.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.http.RequestThreadLocal;

/**
 * Renders the output to the current {@link HttpServletResponse} as stored in the {@link RequestThreadLocal}
 */
public class ServletViewRenderer implements ViewRenderer {
	private ViewResolverRegistry viewResolverRegistry;
	private boolean resolved = false;
	private boolean failIfNoViewResolver;

	public ServletViewRenderer(ViewResolverRegistry viewResolverRegistry, boolean failIfNoViewResolver) {
		super();
		this.viewResolverRegistry = viewResolverRegistry;
		this.failIfNoViewResolver = failIfNoViewResolver;
	}

	@Override
	public void render(Object view) {
		if (resolved) {
			throw new ViewResolutionException("This %s has already been used to render a view, it cannot be reused. Create a new one", this.getClass().getSimpleName());
		}
		resolved = true;
		HttpServletRequest req = RequestThreadLocal.getRequest();
		HttpServletResponse resp = RequestThreadLocal.getResponse();
		ViewResolver<Object> viewResolver = viewResolverRegistry.findViewResolver(view);
		if (viewResolver != null) {
			viewResolver.resolve(req, resp, view);
		} else {
			if (failIfNoViewResolver) {
				throw new ViewResolverNotFoundException("No %s is registered for the view result %s - %s", ViewResolver.class.getSimpleName(), view.getClass().getSimpleName(), view);
			}
		}
	}
}
