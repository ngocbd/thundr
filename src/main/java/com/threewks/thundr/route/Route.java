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
package com.threewks.thundr.route;

import static com.atomicleopard.expressive.Expressive.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.atomicleopard.expressive.EList;
import com.threewks.thundr.http.URLEncoder;

public class Route {
	public static final String PathParameterToken = "\\{(.*?)\\}";
	public static final Pattern PathParameterPattern = Pattern.compile(PathParameterToken);
	// from -> http://www.ietf.org/rfc/rfc1738.txt: "Thus, only alphanumerics, the special characters "$-_.+!*'()," ... may be used unencoded within a URL."
	public static final String AcceptablePathCharacters = "\\w%:@&=+$,!~*'()\\.\\-";
	public static final String AcceptableMultiPathCharacters = AcceptablePathCharacters + "/";

	// The spec allows for request parameters to be encoded in the format /url/url2/url3;key=value;key2=value2
	// This seems to only happen in jetty and tomcat (some versions) when redirects are invoked, particularly if the server
	// hasn't confirmed that the client supports cookies. i.e. redirect -> /go/here;jsessionid=12345678
	// This is a hack implementation which satisfies most current web development needs, further reading on a fuller implementation here:
	// http://www.skorks.com/2010/05/what-every-developer-should-know-about-urls/
	public static final String SemiColonDelimitedRequestParameters = "(?:;.*?)*";

	private String name;
	private String route;
	private Pattern routeMatchRegex;
	private HttpMethod method;
	private EList<String> pathParameters;

	public Route(HttpMethod method, String route, String nameOrNull) {
		super();
		this.name = nameOrNull;
		this.route = route;
		this.method = method;
		this.pathParameters = extractPathParametersFromRoute(route);
		this.routeMatchRegex = Pattern.compile(convertPathStringToRegex(route));
	}

	public String getName() {
		return name;
	}

	public String getRoute() {
		return route;
	}

	public String getRouteMatchRegex() {
		return routeMatchRegex.pattern();
	}

	public HttpMethod getMethod() {
		return method;
	}

	public boolean matches(String routePath) {
		return routeMatchRegex.matcher(routePath).matches();
	}

	public String getReverseRoutePath(Map<String, Object> pathVars) {
		return getReverseRoute(pathVars).getUri();
	}

	public ReverseRoute getReverseRoute(Map<String, Object> pathVars) {
		List<String> missing = list(pathParameters).removeItems(pathVars.keySet());
		if (!missing.isEmpty()) {
			throw new ReverseRouteException("Cannot generate a reverse route for %s - no value(s) supplied for the path variables %s", route, StringUtils.join(missing, ", "));
		}
		if (pathVars.values().contains(null)) {
			throw new ReverseRouteException("Cannot generate a reverse route for %s - one or more parameters were null", route);
		}
		String reverse = route;
		for (Map.Entry<String, Object> entry : pathVars.entrySet()) {
			reverse = reverse.replace("{" + entry.getKey() + "}", URLEncoder.encodePathComponent(entry.getValue().toString()));
		}
		return new ReverseRoute(method, reverse, name);
	}

	public Map<String, String> getPathVars(String routePath) {
		Matcher matcher = routeMatchRegex.matcher(routePath);
		matcher.find();
		int count = matcher.groupCount();
		Map<String, String> results = new HashMap<String, String>();
		for (int i = 1; i <= count; i++) {
			String name = pathParameters.get(i - 1);

			results.put(name, URLEncoder.decodePathComponent(matcher.group(i)));
		}
		return results;
	}

	@Override
	public String toString() {
		String nameString = StringUtils.isBlank(name) ? "" : " (" + name + ")";
		return String.format("%-8s%-50s%16s", method, route, nameString);
	}

	static String convertPathStringToRegex(String route) {
		String wildCardPlaceholder = "____placeholder____";
		route = route.replaceAll("\\*\\*", wildCardPlaceholder);
		route = route.replaceAll("\\*", Matcher.quoteReplacement("[" + AcceptablePathCharacters + "]*?"));
		route = PathParameterPattern.matcher(route).replaceAll(Matcher.quoteReplacement("([" + AcceptablePathCharacters + "]+)"));
		route = route.replaceAll(wildCardPlaceholder, Matcher.quoteReplacement("[" + AcceptableMultiPathCharacters + "]*?"));
		return route + SemiColonDelimitedRequestParameters;
	}

	static EList<String> extractPathParametersFromRoute(String route) {
		Matcher matcher = PathParameterPattern.matcher(route);
		EList<String> results = list();
		while (matcher.find()) {
			String parameter = matcher.group(1);
			results.add(parameter);
		}
		return results;
	}
}
