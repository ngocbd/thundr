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

import static com.atomicleopard.expressive.Expressive.list;

import java.util.List;

import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

public class StringParameterBinder implements ParameterBinder<String> {
	@Override
	public String bind(ParameterBinderRegistry binder, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		List<String> values = pathMap.get(list(parameterDescription.name()));
		return values != null && values.size() > 0 ? values.get(0) : null;
	}

	@Override
	public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
		return parameterDescription.isA(String.class);
	}
}
