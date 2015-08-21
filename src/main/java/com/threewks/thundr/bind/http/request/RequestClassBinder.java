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

import java.util.List;
import java.util.Map;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

public class RequestClassBinder implements Binder {
	public static final List<Class<?>> BoundTypes = Expressive.<Class<?>> list(Request.class, Response.class);

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp) {
		for (Map.Entry<ParameterDescription, Object> binding : bindings.entrySet()) {
			if (binding.getValue() == null) {
				ParameterDescription parameterDescription = binding.getKey();

				Object value = null;
				if (parameterDescription.isA(Request.class)) {
					value = req;
				}
				if (parameterDescription.isA(Response.class)) {
					value = resp;
				}
				bindings.put(parameterDescription, value);
			}
		}
	}
}
