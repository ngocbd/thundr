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
package com.threewks.thundr.bind.path;

import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atomicleopard.expressive.Cast;
import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.bind.Binder;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class PathVariableBinder implements Binder {
	private TransformerManager transformerManager;

	public PathVariableBinder(TransformerManager transformerManager) {
		super();
		this.transformerManager = transformerManager;
	}

	@Override
	public void bindAll(Map<ParameterDescription, Object> bindings, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathVariables) {
		for (ParameterDescription parameterDescription : bindings.keySet()) {
			if (bindings.get(parameterDescription) == null) {
				if (canBindFromPathVariable(parameterDescription)) {
					Object value = bind(parameterDescription, pathVariables);
					bindings.put(parameterDescription, value);
				}
			}
		}
	}

	private boolean canBindFromPathVariable(ParameterDescription parameterDescription) {
		if (isEnum(parameterDescription)) {
			return true;
		}
		Type type = parameterDescription.type();
		Class<?> classType = Cast.as(type, Class.class);
		return classType != null && transformerManager.getTransformer(String.class, classType) != null;
	}

	private boolean isEnum(ParameterDescription parameterDescription) {
		Class<?> classType = parameterDescription.classType();
		return classType != null && classType.isEnum();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object bind(ParameterDescription parameterDescription, Map<String, String> pathVariables) {
		String value = pathVariables.get(parameterDescription.name());
		if (value == null) {
			return null;
		}
		Class<?> type = parameterDescription.classType();
		if (type.isEnum()) {
			return Expressive.Transformers.toEnum((Class<Enum>) type).from(value);
		}
		return transformerManager.transform(String.class, type, value);
	}
}
