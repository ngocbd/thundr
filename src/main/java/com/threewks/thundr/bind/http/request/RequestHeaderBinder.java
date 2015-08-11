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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;

import jodd.util.StringPool;

public class RequestHeaderBinder implements Binder {
	private ParameterBinderRegistry parameterBinderRegistry;

	public RequestHeaderBinder(ParameterBinderRegistry parameterBinderRegistry) {
		super();
		this.parameterBinderRegistry = parameterBinderRegistry;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp, Map<String, String> pathVariables) {
		Map<String, List<String>> parameterMap = createNormalisedHeaderMap(req);
		parameterBinderRegistry.bind(bindings, parameterMap, null);
	}

	private Map<String, List<String>> createNormalisedHeaderMap(Request req) {
		Map<String, List<String>> results = new LinkedHashMap<>();
		Map<String, List<String>> headers = req.getAllHeaders();
		headers.forEach((name, values) -> {
			results.put(normaliseHeaderName(name), values);
		});
		return results;
	}

	String normaliseHeaderName(String header) {
		String capitalised = WordUtils.capitalizeFully(header, '-');
		String withoutDashes = capitalised.replaceAll(StringPool.DASH, StringPool.EMPTY);
		return StringUtils.uncapitalize(withoutDashes);
	}
}
