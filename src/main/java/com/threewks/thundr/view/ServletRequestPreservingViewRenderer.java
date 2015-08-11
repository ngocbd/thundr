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

import static com.atomicleopard.expressive.Expressive.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.http.RequestThreadLocal;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.transformer.TransformerManager;

/**
 * Renders the output to the current {@link HttpServletResponse} as stored in the {@link RequestThreadLocal}
 */
public class ServletRequestPreservingViewRenderer implements ViewRenderer {
	protected ViewResolverRegistry viewResolverRegistry;
	protected TransformerManager transformerManager;

	public ServletRequestPreservingViewRenderer(ViewResolverRegistry viewResolverRegistry, TransformerManager transformerManager) {
		this.transformerManager = transformerManager;
		this.viewResolverRegistry = viewResolverRegistry;
	}

	@Override
	public void render(Request req, Response resp, Object view) {
		/*
		 * Wrapping the request is highly sensitive to the container implementation.
		 * For example, while the Servlet include interface specifies we can pass in a {@link ServletRequestWrapper},
		 * Jetty is having none of it. To avoid ramifications across different application servers, we just reuse the
		 * originating request. To help avoid issues, we restore all attributes after the response is rendered.
		 */
		HttpServletRequest servletRequest = req.getRawRequest(HttpServletRequest.class);
		Map<String, Object> attributes = getAttributes(servletRequest); // save the current set of request attributes
		try {
			ViewResolver<Object> viewResolver = viewResolverRegistry.findViewResolver(view);
			if (viewResolver == null) {
				throw new ViewResolverNotFoundException("No %s is registered for the view result %s - %s", ViewResolver.class.getSimpleName(), view.getClass().getSimpleName(), view);
			} else {
				viewResolver.resolve(req, resp, view);
			}
		} finally {
			setAttributes(servletRequest, attributes); // reapply the attributes, removing any new ones
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getAttributes(HttpServletRequest request) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		if (request != null) {
			for (String name : iterable((Enumeration<String>) request.getAttributeNames())) {
				attributes.put(name, request.getAttribute(name));
			}
		}
		return attributes;
	}

	@SuppressWarnings("unchecked")
	private void setAttributes(HttpServletRequest request, Map<String, Object> attributes) {
		if (request != null) {
			List<String> allNames = list(iterable(request.getAttributeNames())).addItems(attributes.keySet());
			for (String name : allNames) {
				request.setAttribute(name, attributes.get(name));
			}
		}
	}
}
