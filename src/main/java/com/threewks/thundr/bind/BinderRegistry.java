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
package com.threewks.thundr.bind;

import java.util.LinkedHashMap;
import java.util.Map;

import com.threewks.thundr.bind.http.HttpBinder;
import com.threewks.thundr.bind.http.MultipartHttpBinder;
import com.threewks.thundr.bind.http.request.CookieBinder;
import com.threewks.thundr.bind.http.request.RequestClassBinder;
import com.threewks.thundr.bind.http.request.RequestDataBinder;
import com.threewks.thundr.bind.http.request.RequestHeaderBinder;
import com.threewks.thundr.bind.json.GsonBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.bind.path.PathVariableBinder;
import com.threewks.thundr.transformer.TransformerManager;

public class BinderRegistry {
	private Map<Class<? extends Binder>, Binder> methodBinders = new LinkedHashMap<Class<? extends Binder>, Binder>();

	public BinderRegistry() {
	}

	public void registerBinder(Binder binder) {
		methodBinders.put(binder.getClass(), binder);
	}

	public boolean hasBinder(Class<? extends Binder> type) {
		return methodBinders.containsKey(type);
	}

	public void deregisterBinder(Class<? extends Binder> type) {
		methodBinders.remove(type);
	}

	public Iterable<Binder> getRegisteredBinders() {
		return methodBinders.values();
	}

	public static void registerDefaultBinders(BinderRegistry binderRegistry, ParameterBinderRegistry parameterBinderRegistry, TransformerManager transformerManager) {
		binderRegistry.registerBinder(new PathVariableBinder(transformerManager));
		binderRegistry.registerBinder(new RequestClassBinder());
		binderRegistry.registerBinder(new HttpBinder(parameterBinderRegistry));
		binderRegistry.registerBinder(new RequestDataBinder(parameterBinderRegistry));
		binderRegistry.registerBinder(new RequestHeaderBinder(parameterBinderRegistry));
		binderRegistry.registerBinder(new CookieBinder(parameterBinderRegistry));
		// These are last so that we can avoid running them if all parameters are bound by an alternative method
		binderRegistry.registerBinder(new GsonBinder());
		binderRegistry.registerBinder(new MultipartHttpBinder(parameterBinderRegistry));
	}
}
