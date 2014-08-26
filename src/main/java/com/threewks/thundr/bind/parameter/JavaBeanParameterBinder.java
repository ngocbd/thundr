/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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

import java.util.Map;

import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.introspection.TypeIntrospector;
import com.threewks.thundr.transformer.TransformerManager;

import jodd.bean.BeanLoaderManager;

public class JavaBeanParameterBinder implements ParameterBinder<Object> {
	public Object bind(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, TransformerManager transformerManager) {
		Map<String, Object> stringMap = pathMap.toStringMap(parameterDescription.name());
		if (!stringMap.isEmpty()) {
			try {
				Object bean = parameterDescription.classType().newInstance();
				BeanLoaderManager.load(bean, stringMap);
				return bean;
			} catch (Exception e) {
				throw new BindException(e, "Failed to bind onto %s: %s", parameterDescription.classType(), e.getMessage());
			}
		}
		return null;
	}

	@Override
	public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
		Class<?> type = parameterDescription.classType();
		return TypeIntrospector.isAJavabean(type);
	}
}
