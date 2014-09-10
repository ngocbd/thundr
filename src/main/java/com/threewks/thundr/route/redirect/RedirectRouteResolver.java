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
package com.threewks.thundr.route.redirect;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.RouteResolver;
import com.threewks.thundr.route.RouteResolverException;

public class RedirectRouteResolver implements RouteResolver<Redirect> {
	private static final Pattern ActionNamePattern = Pattern.compile("^(?i)redirect:(.+)");

	@Override
	public Object resolve(Redirect action, HttpMethod method, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVars) throws RouteResolverException {
		String redirectTo = action.getRedirectTo(pathVars);
		try {
			resp.sendRedirect(redirectTo);
			return null;
		} catch (Exception e) {
			throw new RouteResolverException(e, "Failed to redirect %s to %s: %s", req.getRequestURI(), redirectTo, e.getMessage());
		}
	}
}
