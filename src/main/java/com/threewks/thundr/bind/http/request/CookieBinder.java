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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
import com.atomicleopard.expressive.transform.CollectionTransformer;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.http.Cookie;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

public class CookieBinder implements Binder {
	public static final List<Class<?>> BoundTypes = Expressive.<Class<?>> list(Cookie.class);
	private static final ETransformer<Cookie, String> ToCookeValue = Expressive.Transformers.toProperty("value", Cookie.class);
	private static final CollectionTransformer<Cookie, String> ToCookeValues = Expressive.Transformers.transformAllUsing(ToCookeValue);
	private ParameterBinderRegistry parameterBinderRegistry;

	public CookieBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp) {
		Map<String, List<Cookie>> cookies = req.getAllCookies();
		if (isNotEmpty(cookies) && bindings.values().contains(null)) {
			Map<String, List<String>> cookieValues = getCookieValues(cookies);
			parameterBinderRegistry.bind(bindings, cookieValues, null);

			for (Map.Entry<ParameterDescription, Object> binding : bindings.entrySet()) {
				ParameterDescription key = binding.getKey();
				if (binding.getValue() == null && key.isA(Cookie.class)) {
					String name = key.name();
					List<Cookie> namedCookies = cookies.get(name);
					Cookie cookie = isNotEmpty(namedCookies) ? namedCookies.get(0) : null;
					bindings.put(key, cookie);
				}
			}
		}
	}

	private Map<String, List<String>> getCookieValues(Map<String, List<Cookie>> lookup) {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		for (Map.Entry<String, List<Cookie>> entry : lookup.entrySet()) {
			result.put(entry.getKey(), ToCookeValues.from(entry.getValue()));
		}
		return result;
	}
}
