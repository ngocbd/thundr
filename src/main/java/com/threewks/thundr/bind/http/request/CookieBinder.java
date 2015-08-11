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
package com.threewks.thundr.bind.http.request;

import static com.atomicleopard.expressive.Expressive.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

// TODO - v3 - need a http.Cookie binder too
public class CookieBinder implements Binder {
	public static final List<Class<?>> BoundTypes = Expressive.<Class<?>> list(Cookie.class);

	private ParameterBinderRegistry parameterBinderRegistry;

	public CookieBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp, Map<String, String> pathVariables) {
		if (req.getCookies() != null && bindings.values().contains(null)) {
			Map<String, List<String>> cookieMap = createCookieMap(req);
			parameterBinderRegistry.bind(bindings, cookieMap, null);

			Map<String, List<Cookie>> lookup = req.getAllCookies();
			for (Map.Entry<ParameterDescription, Object> binding : bindings.entrySet()) {
				ParameterDescription key = binding.getKey();
				if (binding.getValue() == null && key.isA(Cookie.class)) {
					String name = key.name();
					List<Cookie> namedCookies = lookup.get(name);
					Cookie cookie = isNotEmpty(namedCookies) ? namedCookies.get(0) : null;
					bindings.put(key, cookie);
				}
			}
		}
	}

	private Map<String, List<String>> createCookieMap(Request req) {
		Map<String, List<String>> lookup = new HashMap<String, List<String>>();
		for (Cookie cookie : req.getCookies()) {
			String name = cookie.getName();
			String value = cookie.getValue();
			List<String> existing = lookup.get(name);
			if (existing == null) {
				existing = new ArrayList<String>();
				lookup.put(name, existing);
			}
			existing.add(value);
		}
		return lookup;
	}
}
