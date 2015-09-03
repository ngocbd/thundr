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

import java.util.Map;

import com.atomicleopard.expressive.ETransformer;
import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.introspection.TypeIntrospector;
import com.threewks.thundr.transformer.TransformerManager;

import jodd.bean.BeanUtilBean;

public class JavaBeanParameterBinder implements ParameterBinder<Object> {
	@Override
	public Object bind(ParameterBinderRegistry binders, ParameterDescription parameterDescription, RequestDataMap pathMap, final TransformerManager transformerManager) {
		Map<String, Object> stringMap = pathMap.toStringMap(parameterDescription.name());
		if (!stringMap.isEmpty()) {
			try {
				Object bean = parameterDescription.classType().newInstance();
				BeanUtilBean beaner = beanLoader(transformerManager);
				for (Map.Entry<String, Object> entry : stringMap.entrySet()) {
					beaner.setPropertyForcedSilent(bean, entry.getKey(), entry.getValue());
				}
				return bean;
			} catch (Exception e) {
				throw new BindException(e, "Failed to bind onto %s: %s", parameterDescription.classType(), e.getMessage());
			}
		}
		return null;
	}

	public BeanUtilBean beanLoader(final TransformerManager transformerManager) {
		return new TransformerManagerBeanUtilBean(transformerManager);
	}

	@Override
	public boolean willBind(ParameterDescription parameterDescription, TransformerManager transformerManager) {
		Class<?> type = parameterDescription.classType();
		return TypeIntrospector.isAJavabean(type);
	}

	static class TransformerManagerBeanUtilBean extends BeanUtilBean {
		private TransformerManager transformerManager;

		public TransformerManagerBeanUtilBean(TransformerManager transformerManager) {
			super();
			this.transformerManager = transformerManager;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Object convertType(Object value, Class type) {
			Class<?> currentType = value.getClass();
			ETransformer<Object, Object> bestTransformer = transformerManager.getBestTransformer(currentType, type);
			if (bestTransformer == null) {
				throw new BindException("Unable to bind to type %s - no transformer available", type.getName());
			}
			return bestTransformer.from(value);
		}
	}

}
