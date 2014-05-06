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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Filters {
	private Map<String, List<Filter>> filters = new HashMap<String, List<Filter>>();

	public void add(String path, Filter filter) {
		String regex = convertPathStringToRegex(path);
		List<Filter> existing = filters.get(regex);
		if (existing == null) {
			existing = new ArrayList<Filter>();
			filters.put(regex, existing);
		}
		existing.add(filter);
	}

	public void remove(String path, Filter filter) {
		String regex = convertPathStringToRegex(path);
		List<Filter> existing = filters.get(regex);
		if (existing != null) {
			existing.remove(filter);
		}
	}

	public void remove(Filter filter) {
		for (List<Filter> fs : filters.values()) {
			fs.remove(filter);
		}
	}

	public Object before(RouteType routeType, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getPathInfo());
		for (Filter filter : matchingFilters) {
			Object result = filter.before(routeType, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Object after(RouteType routeType, Object view, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getPathInfo());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.after(routeType, view, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Object exception(RouteType routeType, Exception e, HttpServletRequest req, HttpServletResponse resp) {
		List<Filter> matchingFilters = findMatchingFilters(req.getRequestURI());
		for (int i = matchingFilters.size() - 1; i >= 0; i--) {
			Filter filter = matchingFilters.get(i);
			Object result = filter.exception(routeType, e, req, resp);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private List<Filter> findMatchingFilters(String path) {
		List<Filter> filters = new ArrayList<Filter>();
		if (path != null) {
			for (Map.Entry<String, List<Filter>> entry : this.filters.entrySet()) {
				String key = entry.getKey();
				if (path.matches(key)) {
					filters.addAll(entry.getValue());
				}
			}
		}
		return filters;
	}

	static String convertPathStringToRegex(String path) {
		String wildCardPlaceholder = "____placeholder____";
		path = path.replaceAll("\\*\\*", wildCardPlaceholder);
		path = path.replaceAll("\\*", Matcher.quoteReplacement("[" + Route.AcceptablePathCharacters + "]*?"));
		path = Route.PathParameterPattern.matcher(path).replaceAll(Matcher.quoteReplacement("([" + Route.AcceptablePathCharacters + "]+)"));
		path = path.replaceAll(wildCardPlaceholder, Matcher.quoteReplacement("[" + Route.AcceptableMultiPathCharacters + "]*?"));
		return path + Route.SemiColonDelimitedRequestParameters;
	}

	public boolean has(String path, Filter filter) {
		List<Filter> filtersForPath = filters.get(convertPathStringToRegex(path));
		return filtersForPath == null ? false : filtersForPath.contains(filter);
	}
}
