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

import static com.atomicleopard.expressive.Expressive.list;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.route.controller.Controller;

public class Router {
	private Map<Route, RouteResult> actionsForRoutes = new HashMap<Route, RouteResult>();

	private Map<HttpMethod, Map<String, Route>> routes = createRoutesMap();
	private Map<String, Route> namedRoutes = new HashMap<String, Route>();

	private Map<Class<? extends RouteResult>, RouteResolver<?>> actionResolvers = new LinkedHashMap<Class<? extends RouteResult>, RouteResolver<?>>();

	private boolean debug = true;

	public Router get(String route, Class<?> controller, String controllerMethod) {
		return this.get(route, null, controller, controllerMethod);
	}

	public Router get(String route, String name, Class<?> controller, String controllerMethod) {
		return this.get(route, name, new Controller(controller, controllerMethod));
	}

	public <T extends RouteResult> Router get(String route, String name, T action) {
		return this.add(HttpMethod.GET, route, name, action);
	}

	public Router put(String route, Class<?> controller, String controllerMethod) {
		return this.put(route, null, controller, controllerMethod);
	}

	public Router put(String route, String name, Class<?> controller, String controllerMethod) {
		return this.put(route, name, new Controller(controller, controllerMethod));
	}

	public <T extends RouteResult> Router put(String route, String name, T action) {
		return this.add(HttpMethod.PUT, route, name, action);
	}

	public Router post(String route, Class<?> controller, String controllerMethod) {
		return this.post(route, null, controller, controllerMethod);
	}

	public Router post(String route, String name, Class<?> controller, String controllerMethod) {
		return this.post(route, name, new Controller(controller, controllerMethod));
	}

	public <T extends RouteResult> Router post(String route, String name, T action) {
		return this.add(HttpMethod.POST, route, name, action);
	}

	public Router delete(String route, Class<?> controller, String method) {
		return this.delete(route, null, controller, method);
	}

	public Router delete(String route, String name, Class<?> controller, String controllerMethod) {
		return this.delete(route, name, new Controller(controller, controllerMethod));
	}

	public <T extends RouteResult> Router delete(String route, String name, T action) {
		return this.add(HttpMethod.PUT, route, name, action);
	}

	public Router add(HttpMethod method, String route, Class<?> controller, String controllerMethod) {
		return this.add(method, route, null, controller, controllerMethod);
	}

	public Router add(HttpMethod method, String route, String name, Class<?> controller, String controllerMethod) {
		return this.add(method, route, name, new Controller(controller, controllerMethod));
	}

	public <T extends RouteResult> Router add(HttpMethod httpMethod, String routePath, String name, T action) {
		Route route = new Route(httpMethod, routePath, name);
		String path = route.getRouteMatchRegex();
		Map<String, Route> routesForRouteType = this.routes.get(httpMethod);
		if (routesForRouteType.containsKey(path)) {
			Route existingRoute = routesForRouteType.get(path);
			throw new RouteException("Unable to add the route '%s %s' - the route '%s %s' already exists which matches the same pattern", route.getRouteType(), route.getRoute(),
					existingRoute.getRouteType(), existingRoute.getRoute());
		}
		if (StringUtils.isNotBlank(name)) {
			if (namedRoutes.containsKey(name)) {
				Route existingRoute = namedRoutes.get(name);
				throw new RouteException("Unable to add the route '%s %s' with the name '%s' - the route '%s %s' has already been registered with this name", route.getRouteType(), route.getRoute(),
						name, existingRoute.getRouteType(), existingRoute.getRoute());
			}
			this.namedRoutes.put(name, route);
		}
		routesForRouteType.put(path, route);
		this.actionsForRoutes.put(route, action);
		return this;
	}

	public Route getRoute(String name) {
		return namedRoutes.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T extends RouteResult> Object invoke(String routePath, HttpMethod httpMethod, HttpServletRequest req, HttpServletResponse resp) {
		Logger.debug("Requesting '%s'", routePath);
		Route route = findMatchingRoute(routePath, httpMethod);
		if (route != null) {
			T action = (T) actionsForRoutes.get(route);
			return resolveAction(routePath, httpMethod, req, resp, route, action);
		}
		String debugString = debug ? listRoutes() : "";
		throw new RouteNotFoundException("No route matching the request %s %s\n%s", httpMethod, routePath, debugString);
	}

	public Route findMatchingRoute(String routePath, HttpMethod method) {
		Map<String, Route> routesForRouteType = routes.get(method);
		for (Route route : routesForRouteType.values()) {
			if (route.matches(routePath)) {
				return route;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T extends RouteResult> Object resolveAction(final String routePath, final HttpMethod method, final HttpServletRequest req, final HttpServletResponse resp, final Route route,
			final T action) {
		Map<String, String> pathVars = route.getPathVars(routePath);
		RouteResolver<T> actionResolver = (RouteResolver<T>) actionResolvers.get(action.getClass());
		Object resolve = actionResolver.resolve(action, method, req, resp, pathVars);
		return resolve;
	}

	public boolean isEmpty() {
		return actionsForRoutes.isEmpty();
	}

	private static final String routeDisplayFormat = "%s: %s\n";

	public String listRoutes() {
		Set<String> allRoutes = new HashSet<String>();
		for (Map<String, Route> routeEntries : routes.values()) {
			allRoutes.addAll(routeEntries.keySet());
		}
		List<String> allRouteNames = list(allRoutes);
		Collections.sort(allRouteNames);

		StringBuilder sb = new StringBuilder();
		for (String route : allRouteNames) {
			for (HttpMethod method : HttpMethod.all()) {
				Map<String, Route> routesForType = routes.get(method);
				if (routesForType.containsKey(route)) {
					Route actualRoute = routesForType.get(route);
					RouteResult action = this.actionsForRoutes.get(actualRoute);
					sb.append(String.format(routeDisplayFormat, actualRoute, action));
				}
			}
		}
		return sb.toString();
	}

	public <A extends RouteResult> void addResolver(Class<A> actionType, RouteResolver<A> actionResolver) {
		actionResolvers.put(actionType, actionResolver);
		Logger.debug("Added action resolver %s for actions of type %s", actionResolver.getClass().getSimpleName(), actionType);
	}

	@SuppressWarnings("unchecked")
	public <A extends RouteResult> RouteResolver<A> getResolver(Class<A> actionType) {
		return (RouteResolver<A>) actionResolvers.get(actionType);
	}

	private Map<HttpMethod, Map<String, Route>> createRoutesMap() {
		Map<HttpMethod, Map<String, Route>> routesMap = new HashMap<HttpMethod, Map<String, Route>>();
		for (HttpMethod type : HttpMethod.all()) {
			routesMap.put(type, new LinkedHashMap<String, Route>());
		}
		return routesMap;
	}

}
