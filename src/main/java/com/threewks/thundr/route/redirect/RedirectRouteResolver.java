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
package com.threewks.thundr.route.redirect;

import java.util.Map;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.RouteResolver;
import com.threewks.thundr.route.RouteResolverException;

public class RedirectRouteResolver implements RouteResolver<Redirect> {
	@Override
	public Object resolve(Redirect redirect, Request req, Response resp) throws RouteResolverException {
		Route route = req.getRoute();
		Map<String, String> pathVars = route.getPathVars(req.getRequestPath());
		String redirectTo = redirect.getRedirectTo(pathVars);
		try {
			resp
				.withStatusCode(StatusCode.Found)
				.withHeader(Header.Location, redirectTo)
				.finaliseHeaders();
			return null;
		} catch (Exception e) {
			throw new RouteResolverException(e, "Failed to redirect %s to %s: %s", req.getRequestPath(), redirectTo, e.getMessage());
		}
	}
}
