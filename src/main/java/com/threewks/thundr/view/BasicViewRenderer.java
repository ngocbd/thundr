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
package com.threewks.thundr.view;

import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

/**
 * Renders the view into the given response object.
 */
public class BasicViewRenderer implements ViewRenderer {
	private ViewResolverRegistry viewResolverRegistry;

	public BasicViewRenderer(ViewResolverRegistry viewResolverRegistry) {
		this.viewResolverRegistry = viewResolverRegistry;
	}

	@Override
	public void render(Request request, Response response, Object view) {
		ViewResolver<Object> viewResolver = viewResolverRegistry.findViewResolver(view);
		if (viewResolver != null) {
			viewResolver.resolve(request, response, view);
		} else {
			throw new ViewResolverNotFoundException("No %s is registered for the view result %s - %s", ViewResolver.class.getSimpleName(), view.getClass().getSimpleName(), view);
		}
	}
}
