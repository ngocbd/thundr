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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

public class RequestDataBinder implements Binder {
	private ParameterBinderRegistry parameterBinderRegistry;

	public RequestDataBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	// TODO - NAO - v3 - a much more comprehensive test suite around the behaviour of normalising names is needed.
	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp) {
		Map<String, Object> requestData = req.getAllData();
		Map<String, Object> normalisedKeys = RequestHeaderBinder.normaliseKeysToJavaVarNames(requestData);

		Map<String, List<String>> datamap = createListMap(normalisedKeys);
		parameterBinderRegistry.bind(bindings, datamap, null);

		for (Map.Entry<ParameterDescription, Object> binding : bindings.entrySet()) {
			ParameterDescription key = binding.getKey();
			String name = key.name();
			Object value = normalisedKeys.get(name);
			if (binding.getValue() == null && value != null) {
				if (key.isA(value.getClass())) {
					bindings.put(key, value);
				}
			}
		}
	}

	private Map<String, List<String>> createListMap(Map<String, Object> normalisedKeys) {
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : normalisedKeys.entrySet()) {
			if (entry.getValue() instanceof String) {
				map.put(entry.getKey(), Collections.singletonList((String) entry.getValue()));
			}
		}
		return map;
	}
}
