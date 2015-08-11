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
package com.threewks.thundr;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.http.Header;
import com.threewks.thundr.injection.InjectionContext;
import com.threewks.thundr.request.MutableRequestContainer;
import com.threewks.thundr.request.servlet.ServletRequest;
import com.threewks.thundr.request.servlet.ServletResponse;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.view.BasicViewRenderer;
import com.threewks.thundr.view.ViewRenderer;
import com.threewks.thundr.view.ViewResolverRegistry;

@WebServlet(name = "thundr", urlPatterns = { "/" }, asyncSupported = true)
public class ThundrServlet extends HttpServlet {
	private static final long serialVersionUID = -1;
	protected Thundr thundr;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			thundr = createAndStartThundr();
			// TODO - v3 - this should be part of the 'servletmodule'
			// servletContext.setAttribute("injectionContext", injectionContext);
			// injectionContext.inject(servletContext).as(ServletContext.class);
		} catch (RuntimeException e) {
			throw new ServletException("Failed to initialse thundr: " + e.getMessage(), e);
		}
	}

	@Override
	public void destroy() {
		thundr.stop();
		super.destroy();
	}

	/**
	 * Factory method for creating the thundr executor, you can override this if you need to
	 * customise the implementation.
	 * 
	 * @return
	 */
	protected Thundr createAndStartThundr() {
		Thundr thundr = new Thundr();
		thundr.start();
		return thundr;
	}

	protected void applyRoute(final HttpMethod method, final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		InjectionContext injectionContext = thundr.getInjectionContext();
		TransformerManager transformerManager = injectionContext.get(TransformerManager.class);
		MutableRequestContainer requestContainer = injectionContext.get(MutableRequestContainer.class);
		ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);
		ViewRenderer viewRenderer = createViewRenderer(viewResolverRegistry);
		ServletRequest request = new ServletRequest(req, method);
		ServletResponse response = new ServletResponse(transformerManager, resp);
		try {
			requestContainer.set(request, response);
			thundr.applyRoute(request, response, viewRenderer);
		} finally {
			requestContainer.clear();
		}
	}

	protected ViewRenderer createViewRenderer(ViewResolverRegistry viewResolverRegistry) {
		return new BasicViewRenderer(viewResolverRegistry);
	}

	/*
	 * This method is here so that the basic servlet HEAD functionality continues to work;
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		applyRoute(HttpMethod.GET, req, resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean handled = customService(req, resp);
		if (!handled) {
			String httpMethod = determineMethod(req);
			HttpMethod method = HttpMethod.from(httpMethod);
			if (method == HttpMethod.HEAD) {
				doHead(req, resp);
			} else if (method != null) {
				applyRoute(method, req, resp);
			} else {
				// thundr doesnt deal with these
				resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Request method '" + httpMethod + "' not implemented.");
			}
		}
	}

	/**
	 * Given a request, determines the method (i.e. GET, PUT, POST etc)
	 * 
	 * @param req
	 * @return
	 */
	protected String determineMethod(HttpServletRequest req) {
		String method = req.getMethod();
		if (HttpMethod.POST.matches(method)) {
			String methodOverride = req.getHeader(Header.XHttpMethodOverride);
			String methodOverride2 = getParameterCaseInsensitive(req, "_method");

			if (methodOverride != null) {
				method = methodOverride;
			}
			if (methodOverride2 != null) {
				method = methodOverride2;
			}
		}
		return method;
	}

	/**
	 * A custom extensionpoint which allows overriding servlets to handle requests/route types that thundr currently does not.
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
	protected boolean customService(HttpServletRequest req, HttpServletResponse resp) {
		return false;
	}

	protected String getParameterCaseInsensitive(HttpServletRequest req, String parameterName) {
		Iterable<String> iterable = Expressive.<String> iterable(req.getParameterNames());
		for (String parameter : iterable) {
			if (parameterName.equalsIgnoreCase(parameter)) {
				return req.getParameter(parameter);
			}
		}
		return null;
	}
}