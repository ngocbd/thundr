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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.module.DependencyRegistry;
import com.threewks.thundr.transformer.TransformerModule;

public class ParameterBindingModuleTest {
	private ParameterBindingModule module = new ParameterBindingModule();

	@Test
	public void shouldDependOnTransformerModule() {
		DependencyRegistry dependencyRegistry = new DependencyRegistry();
		module.requires(dependencyRegistry);
		assertThat(dependencyRegistry.hasDependency(TransformerModule.class), is(true));
	}

	@Test
	public void shouldProvideParameterBinderRegistryWithDefaultsConfigured() {

		UpdatableInjectionContext injectionContext = new InjectionContextImpl();
		module.configure(injectionContext);

		ParameterBinderRegistry parameterBinderRegistry = injectionContext.get(ParameterBinderRegistry.class);
		assertThat(parameterBinderRegistry, is(notNullValue()));
	}
}
