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
package com.threewks.thundr.bind;

import com.google.gson.GsonBuilder;
import com.threewks.thundr.bind.http.HttpBinder;
import com.threewks.thundr.bind.http.MultipartHttpBinder;
import com.threewks.thundr.bind.http.request.CookieBinder;
import com.threewks.thundr.bind.http.request.RequestClassBinder;
import com.threewks.thundr.bind.http.request.RequestDataBinder;
import com.threewks.thundr.bind.http.request.RequestHeaderBinder;
import com.threewks.thundr.bind.json.GsonBinder;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.bind.parameter.ParameterBindingModule;
import com.threewks.thundr.bind.path.PathVariableBinder;
import com.threewks.thundr.injection.BaseModule;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.transformer.TransformerManager;
import com.threewks.thundr.transformer.TransformerModule;

public class BinderModule extends BaseModule {
	@Override
	public void requires(DependencyRegistry dependencyRegistry) {
		super.requires(dependencyRegistry);
		dependencyRegistry.addDependency(ParameterBindingModule.class);
		dependencyRegistry.addDependency(TransformerModule.class);
	}

	@Override
	public void initialise(UpdatableInjectionContext injectionContext) {
		super.initialise(injectionContext);
		BinderRegistry binderRegistry = new BinderRegistry();
		injectionContext.inject(binderRegistry).as(BinderRegistry.class);

		ParameterBinderRegistry parameterBinderRegistry = injectionContext.get(ParameterBinderRegistry.class);
		TransformerManager transformerManager = injectionContext.get(TransformerManager.class);

		addDefaultBinders(binderRegistry, parameterBinderRegistry, transformerManager);
	}

	@Override
	public void start(UpdatableInjectionContext injectionContext) {
		super.start(injectionContext);
		ParameterBinderRegistry parameterBinderRegistry = injectionContext.get(ParameterBinderRegistry.class);
		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);
		addBodyConsumingBinders(injectionContext, binderRegistry, parameterBinderRegistry);
	}

	public static BinderRegistry addDefaultBinders(BinderRegistry binderRegistry, ParameterBinderRegistry parameterBinderRegistry, TransformerManager transformerManager) {
		binderRegistry.add(new PathVariableBinder(transformerManager));
		binderRegistry.add(new RequestClassBinder());
		binderRegistry.add(new HttpBinder(parameterBinderRegistry));
		binderRegistry.add(new RequestDataBinder(parameterBinderRegistry));
		binderRegistry.add(new RequestHeaderBinder(parameterBinderRegistry));
		binderRegistry.add(new CookieBinder(parameterBinderRegistry));
		return binderRegistry;
	}

	public static void addBodyConsumingBinders(UpdatableInjectionContext injectionContext, BinderRegistry binderRegistry, ParameterBinderRegistry parameterBinderRegistry) {
		GsonBuilder gsonBuilder = injectionContext.get(GsonBuilder.class);
		binderRegistry.add(new GsonBinder(gsonBuilder));
		binderRegistry.add(new MultipartHttpBinder(parameterBinderRegistry));
	}
}
