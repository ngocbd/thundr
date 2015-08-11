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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.request.servlet.ServletRequest;

public class SessionAttributeBinder implements Binder {

	private ParameterBinderRegistry parameterBinderRegistry;

	public SessionAttributeBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp, Map<String, String> pathVariables) {
		HttpServletRequest httpServletRequest = req.getRawRequest(HttpServletRequest.class);
		HttpSession session = httpServletRequest == null ? null : httpServletRequest.getSession();
		if (session != null) {
			Map<String, List<String>> requestAttributes = createStringSessionAttributes(session);
			parameterBinderRegistry.bind(bindings, requestAttributes, null);

			for (Map.Entry<ParameterDescription, Object> binding : bindings.entrySet()) {
				ParameterDescription key = binding.getKey();
				String name = key.name();
				Object value = session.getAttribute(name);
				if (binding.getValue() == null && value != null && key.isA(value.getClass())) {
					bindings.put(key, value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> createStringSessionAttributes(HttpSession session) {
		Map<String, List<String>> results = new LinkedHashMap<>();
		Enumeration<String> attributeNames = session.getAttributeNames();
		if (attributeNames != null) {
			for (String name : Expressive.iterable(attributeNames)) {
				Object value = session.getAttribute(name);
				if (value instanceof String) {
					results.put(name, Arrays.asList((String) value));
				}
			}
		}
		return results;
	}
}
