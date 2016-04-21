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
package com.threewks.thundr.bind.http.request;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	public void bindAll(Map<ParameterDescription, Object> bindings, Request req, Response resp) {
		Map<String, List<String>> parameterMap = createNormalisedHeaderMap(req);
		parameterBinderRegistry.bind(bindings, parameterMap, null);
	}

	private Map<String, List<String>> createNormalisedHeaderMap(Request req) {
		Map<String, List<String>> headers = req.getAllHeaders();
		return normaliseKeysToJavaVarNames(headers);
	}

	public static <V> Map<String, V> normaliseKeysToJavaVarNames(Map<String, V> input) {
		Map<String, V> results = new LinkedHashMap<>();
		for (Map.Entry<String, V> entry : input.entrySet()) {
			results.put(normaliseToJavaVarName(entry.getKey()), entry.getValue());
		}
		return results;
	}

	public static String normaliseToJavaVarName(String header) {
		char[] chars = header.toCharArray();

		// Replace all invalid java identifier chars with space
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			sb.append(Character.isJavaIdentifierPart(chars[i]) ? chars[i] : StringPool.SPACE);
		}
		// determine if the first character is not valid
		String prepend = Character.isJavaIdentifierStart(chars[0]) ? "" : StringPool.UNDERSCORE;

		String javaTerm = WordUtils.capitalizeFully(sb.toString(), ' ');
		javaTerm = javaTerm.replaceAll(StringPool.SPACE, StringPool.EMPTY);
		javaTerm = WordUtils.uncapitalize(javaTerm);
		return prepend + javaTerm;
	}
}
