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
package com.threewks.thundr.route;

import static com.atomicleopard.expressive.Expressive.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public enum HttpMethod {
	HEAD,
	GET,
	POST,
	PUT,
	PATCH,
	DELETE, 
	OPTIONS;

	private static final List<HttpMethod> all = list(HttpMethod.values());
	private static final Map<String, HttpMethod> lookup = createLookup();

	public static List<HttpMethod> all() {
		return all;
	}

	private static Map<String, HttpMethod> createLookup() {
		Map<String, HttpMethod> map = new HashMap<String, HttpMethod>();
		for (HttpMethod method : all) {
			map.put(method.name(), method);
		}

		return map;
	}

	public static HttpMethod from(String method) {
		return lookup.get(StringUtils.trimToEmpty(StringUtils.upperCase(method)));
	}

	public boolean matches(String method) {
		return name().equalsIgnoreCase(method);
	}
}
