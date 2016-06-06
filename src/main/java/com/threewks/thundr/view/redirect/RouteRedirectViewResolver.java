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
package com.threewks.thundr.view.redirect;

import com.threewks.thundr.http.Header;
import com.threewks.thundr.http.StatusCode;
import com.threewks.thundr.http.URLEncoder;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.ReverseRouteException;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.Router;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;

public class RouteRedirectViewResolver implements ViewResolver<RouteRedirectView> {
	private Router router;

	public RouteRedirectViewResolver(Router router) {
		this.router = router;
	}

	@Override
	public void resolve(Request req, Response resp, RouteRedirectView viewResult) {
		String routeName = viewResult.getRoute();
		Route route = this.router.getNamedRoute(routeName);
		if (route == null) {
			throw new ViewResolutionException("Cannot redirect to the route named '%s': no route with this name exists", routeName);
		}
		String reverseRoute = getReverseRoute(viewResult, route);
		String queryString = URLEncoder.encodeQueryString(viewResult.getQueryParameters());
		// @formatter:off
		resp.withStatusCode(StatusCode.Found)
			.withHeader(Header.Location, reverseRoute + queryString)
			.finaliseHeaders();
		// @formatter:on
	}

	private String getReverseRoute(RouteRedirectView viewResult, Route route) {
		try {
			return route.getReverseRoutePath(viewResult.getPathVariables());
		} catch (ReverseRouteException e) {
			throw new ViewResolutionException(e, "Failed to redirect to route '%s': %s", route.getRoute(), e.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
