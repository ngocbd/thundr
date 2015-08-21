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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Test;

import com.atomicleopard.expressive.EList;
import com.atomicleopard.expressive.ETransformer;
import com.atomicleopard.expressive.Expressive;
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
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.transformer.TransformerManager;

public class BinderModuleTest {
	ParameterBindingModule parameterBindingModule = new ParameterBindingModule();
	BinderModule binderModule = new BinderModule();

	@Test
	public void shouldDependOnParameterBindingModule() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		binderModule.requires(dependencyRegistry);

		assertThat(dependencyRegistry.hasDependency(ParameterBindingModule.class), is(true));
	}

	@Test
	public void shouldInjectTheBinderRegistry() {
		UpdatableInjectionContext injectionContext = new InjectionContextImpl();

		binderModule.initialise(injectionContext);

		assertThat(injectionContext.contains(BinderRegistry.class), is(true));
	}

	@Test
	public void shouldConfigureBinderRegistryWithBasicBinders() {
		UpdatableInjectionContext injectionContext = new InjectionContextImpl();
		injectionContext.inject(TransformerManager.createWithDefaults()).as(TransformerManager.class);
		injectionContext.inject(ParameterBinderRegistry.class).as(ParameterBinderRegistry.class);

		binderModule.initialise(injectionContext);
		binderModule.configure(injectionContext);

		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);
		Iterable<Binder> binders = binderRegistry.list();
		EList<Class<? extends Binder>> types = Expressive.Transformers.transformAllUsing(new ToClass<Binder>()).from(binders);

		assertThat(types, hasItem(PathVariableBinder.class));
		assertThat(types, hasItem(RequestClassBinder.class));
		assertThat(types, hasItem(RequestDataBinder.class));
		assertThat(types, hasItem(RequestHeaderBinder.class));
		assertThat(types, hasItem(HttpBinder.class));
		assertThat(types, hasItem(CookieBinder.class));
		assertThat(types, hasItem(GsonBinder.class));
		assertThat(types, hasItem(MultipartHttpBinder.class));
	}

	@Test
	public void shouldRegisterDefaultBindersInOrder() {
		UpdatableInjectionContext injectionContext = new InjectionContextImpl();
		binderModule.initialise(injectionContext);
		BinderRegistry binderRegistry = injectionContext.get(BinderRegistry.class);

		Iterator<Binder> iterator = binderRegistry.list().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof PathVariableBinder, is(true));
		assertThat(iterator.next() instanceof RequestClassBinder, is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.next() instanceof RequestDataBinder, is(true));
		assertThat(iterator.next() instanceof RequestHeaderBinder, is(true));
		assertThat(iterator.next() instanceof CookieBinder, is(true));
		assertThat(iterator.hasNext(), is(false));

		binderModule.configure(injectionContext);
		iterator = binderRegistry.list().iterator();
		assertThat(iterator.hasNext(), is(true));
		assertThat(iterator.next() instanceof PathVariableBinder, is(true));
		assertThat(iterator.next() instanceof RequestClassBinder, is(true));
		assertThat(iterator.next() instanceof HttpBinder, is(true));
		assertThat(iterator.next() instanceof RequestDataBinder, is(true));
		assertThat(iterator.next() instanceof RequestHeaderBinder, is(true));
		assertThat(iterator.next() instanceof CookieBinder, is(true));
		assertThat(iterator.next() instanceof GsonBinder, is(true));
		assertThat(iterator.next() instanceof MultipartHttpBinder, is(true));
		assertThat(iterator.hasNext(), is(false));
	}

	@SuppressWarnings("unchecked")
	private static final class ToClass<T> implements ETransformer<T, Class<? extends T>> {
		@Override
		public Class<T> from(T from) {
			return (Class<T>) from.getClass();
		}
	}
}
