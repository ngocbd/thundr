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
package com.threewks.thundr.bind.parameter;

import java.util.List;

import com.atomicleopard.expressive.Expressive;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class EnumParameterBinder implements ParameterBinder<Object> {
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object bind(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		List<String> stringValues = pathMap.get(parameterDescription.name());
		String stringValue = stringValues == null || stringValues.size() == 0 ? null : stringValues.get(0);
		Class<?> classType = parameterDescription.classType();
		Class<Enum> enumType = (Class<Enum>) classType;
		return Expressive.Transformers.toEnum(enumType).from(stringValue);
	}

	@Override
	public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
		return parameterDescription.classType().isEnum();
	}

}
